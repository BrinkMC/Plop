package com.brinkmc.plop

import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.command.admin.*
import com.brinkmc.plop.shared.command.plot.CommandPlotTp
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetHome
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetPoint
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotVisitClose
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotVisitOpen
import com.brinkmc.plop.shared.command.plot.preview.CommandPlotPreview
import com.brinkmc.plop.shared.command.plot.visit.CommandPlotVisit
import com.brinkmc.plop.shared.command.processors.GeneralSuggestionProcessor
import com.brinkmc.plop.shared.command.shop.CommandShopList
import com.brinkmc.plop.shared.command.shop.CommandTrade
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.config.configs.MainConfig
import com.brinkmc.plop.shared.config.configs.PlotConfig
import com.brinkmc.plop.shared.config.configs.SQLConfig
import com.brinkmc.plop.shared.config.configs.ShopConfig
import com.brinkmc.plop.shared.config.configs.TotemConfig
import com.brinkmc.plop.shared.gui.preview.HotbarPreview
import com.brinkmc.plop.shared.hooks.Economy
import com.brinkmc.plop.shared.hooks.Guilds
import com.brinkmc.plop.shared.hooks.MythicMobs
import com.brinkmc.plop.shared.hooks.ProtocolLib
import com.brinkmc.plop.shared.hooks.WorldGuard
import com.brinkmc.plop.shared.hooks.listener.GeneralListener
import com.brinkmc.plop.shared.hooks.listener.MovementListener
import com.brinkmc.plop.shared.hooks.listener.MythicListener
import com.brinkmc.plop.shared.hooks.listener.PlayerInteract
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shared.util.PlopMessageSource
import com.brinkmc.plop.shop.Shops
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.google.gson.Gson
import com.noxcrew.interfaces.InterfacesListeners
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import java.io.File


class Plop : State, SuspendingJavaPlugin() {

    private val plugin = this

    lateinit var plots: Plots
    lateinit var shops: Shops
    lateinit var menus: Menus
    lateinit var DB: HikariManager

    private lateinit var commandManager: PaperCommandManager<Source>
    private lateinit var annotationParser: AnnotationParser<Source>
    private lateinit var messageSource: PlopMessageSource
    private lateinit var messageService: MessageService
    private lateinit var configManager: ConfigReader

    // Hooks
    lateinit var hooks: Hooks


    private lateinit var generalListener: GeneralListener
    private lateinit var mythicListener: MythicListener
    private lateinit var movementListener: MovementListener
    private lateinit var playerInteractListener: PlayerInteract

    lateinit var gson: Gson

    override suspend fun onEnableAsync() {
        load()
    }

    override suspend fun onDisableAsync() {
        //TODO ascertain order to kill
        plots.kill()
        shops.kill()
        configManager.kill()
        DB.kill()
    }

    /*
    Putting everything inside a load function results in easy reload of the entire plugin if necessary based off of the state system
     */
    override suspend fun load() {
        val configFolder = plugin.dataFolder
        if (!configFolder.exists()) {
            configFolder.mkdirs() // Create the plugins/Plop folder if it doesn't exist
        } // I can't believe I have to do this

        // Load configs initially to get all necessary data
        plugin.slF4JLogger.info("Initiating config manager")
        configManager = ConfigReader(plugin)
        configManager.load()
        plugin.slF4JLogger.info("Finished loading config manager")

        DB = HikariManager(plugin)
        DB.load()

        // Load the two parts of the plugin
        plugin.slF4JLogger.info("Initiating plots")
        plots = Plots(plugin)
        plots.load()
        plugin.slF4JLogger.info("Initiating shops")
        shops = Shops(plugin)
        shops.load()

        // Get instance of hooks
        plugin.slF4JLogger.info("Hooking into other plugins")
        hooks = Hooks(plugin)

        // Enable all menus
        plugin.slF4JLogger.info("Creating menus and hotbars")
        menus = Menus(plugin)

        // Enable gson library for messages
        plugin.gson = Gson()

        // Register listener
        loadListeners()

        // Finally enable commands
        loadCmds()
    }

    override suspend fun kill() {
        plots.kill()
        shops.kill()
    }

    private fun loadListeners() {
        InterfacesListeners.install(this)

        plugin.slF4JLogger.info("Initiating listeners")
        generalListener = GeneralListener(this)
        movementListener = MovementListener(this)
        mythicListener = MythicListener(this)
        playerInteractListener = PlayerInteract(this)

        // Listeners
        listOf(
            generalListener,
            mythicListener,
            movementListener,
            playerInteractListener
        ).forEach { listener -> Bukkit.getServer().pluginManager.registerSuspendingEvents(listener, this) }
        plugin.slF4JLogger.info("Finished hooking listeners")
    }

    private fun loadCmds() {
        commandManager = PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(plugin)

        annotationParser = AnnotationParser(commandManager, Source::class.java)
        // The plugin will be using coroutines extensively for scheduling
        annotationParser.installCoroutineSupport()

        annotationParser.parse(GeneralSuggestionProcessor(plugin))

        listOf(
            CommandClaimPlot(plugin),
            CommandCreateShop(plugin),
            CommandDeleteShop(plugin),
            CommandTransferPlot(plugin),
            CommandUnclaimPlot(plugin),
            CommandClaimPlot(plugin),
            CommandPlotSetHome(plugin),
            CommandPlotSetPoint(plugin),
            CommandPlotVisitClose(plugin),
            CommandPlotVisitOpen(plugin),
            CommandPlotPreview(plugin),
            CommandPlotVisit(plugin),
            CommandPlotTp(plugin),
            CommandShopList(plugin),
            CommandTrade(plugin)
        ).forEach { command ->
            annotationParser.parse(command)
        }
    }

    fun getFile(fileName: String): File? {
        return File(plugin.dataFolder, fileName)
    }

    fun getMessageService(): MessageService {
        return this.messageService
    }

    fun getConfigManager(): ConfigReader {
        return this.configManager
    }

    fun plopMessageSource(): PlopMessageSource {
        return this.messageSource
    }

    val namespacedKey: NamespacedKey
        get() = NamespacedKey(plugin, "plop")



    // Enable hooks
    class Hooks(val plugin: Plop): State {
        val guilds = Guilds(plugin)
        val protocolLib = ProtocolLib(plugin)
        val mythicMobs = MythicMobs(plugin)
        val worldGuard = WorldGuard(plugin)
        val economy = Economy(plugin)

        override suspend fun load() {
            listOf(
                guilds,
                protocolLib,
                mythicMobs,
                worldGuard,
                economy
            ).forEach { hook -> hook.load() }
        }

        override suspend fun kill() {
            listOf(
                guilds,
                protocolLib,
                mythicMobs,
                worldGuard,
                economy
            ).forEach { hook -> hook.kill() }
        }
    }

    class Menus(val plugin: Plop) {
        val hotbarPreview = HotbarPreview(plugin)

        suspend fun openHotbarPreview(player: Player, prev: InterfaceView? = null): InterfaceView {
            return hotbarPreview.create().open(player, prev)
        }
    }
}