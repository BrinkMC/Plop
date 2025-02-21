package com.brinkmc.plop

import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.command.admin.*
import com.brinkmc.plop.shared.command.plot.general.CommandPlotHome
import com.brinkmc.plop.shared.command.plot.general.CommandPlotVisit
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetEntrance
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetHome
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotVisitToggle
import com.brinkmc.plop.shared.command.plot.preview.CommandPlotPreview
import com.brinkmc.plop.shared.command.processors.GeneralSuggestionProcessor
import com.brinkmc.plop.shared.command.utils.PlotTypeParser
import com.brinkmc.plop.shared.config.ConfigReader
import com.brinkmc.plop.shared.gui.nexus.MenuNexusMain
import com.brinkmc.plop.shared.gui.nexus.MenuPlotLogs
import com.brinkmc.plop.shared.gui.nexus.MenuTotemList
import com.brinkmc.plop.shared.gui.nexus.MenuUpgrade
import com.brinkmc.plop.shared.gui.preview.HotbarPreview
import com.brinkmc.plop.shared.gui.selector.SelectionOtherMenu
import com.brinkmc.plop.shared.gui.selector.SelectionSelfMenu
import com.brinkmc.plop.shared.gui.visit.MenuPlotList
import com.brinkmc.plop.shared.hooks.Economy
import com.brinkmc.plop.shared.hooks.Guilds
import com.brinkmc.plop.shared.hooks.MythicMobs
import com.brinkmc.plop.shared.hooks.PacketEvents
import com.brinkmc.plop.shared.hooks.WorldGuard
import com.brinkmc.plop.shared.hooks.listener.GeneralListener
import com.brinkmc.plop.shared.hooks.listener.PreviewListener
import com.brinkmc.plop.shared.hooks.listener.MythicListener
import com.brinkmc.plop.shared.hooks.listener.PlayerInteract
import com.brinkmc.plop.shared.storage.HikariManager
import com.brinkmc.plop.shared.util.LocationUtils
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shared.util.PlopMessageSource
import com.brinkmc.plop.shop.Shops
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.google.gson.Gson
import com.noxcrew.interfaces.InterfacesListeners
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import java.io.File


class Plop : State, SuspendingJavaPlugin() {

    private val plugin = this

    lateinit var plots: Plots
    lateinit var shops: Shops
    lateinit var menus: Menus
    lateinit var DB: HikariManager

    private lateinit var commandManager: PaperCommandManager<CommandSourceStack>
    private lateinit var annotationParser: AnnotationParser<CommandSourceStack>
    private lateinit var messageSource: PlopMessageSource
    private lateinit var messageService: MessageService
    private lateinit var configManager: ConfigReader

    // Hooks
    lateinit var hooks: Hooks


    private lateinit var generalListener: GeneralListener
    private lateinit var mythicListener: MythicListener
    private lateinit var previewListener: PreviewListener
    private lateinit var playerInteractListener: PlayerInteract

    lateinit var gson: Gson


    override suspend fun onLoadAsync() {
        com.github.retrooper.packetevents.PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        com.github.retrooper.packetevents.PacketEvents.getAPI().load();
    }

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

        // Load messages
        messageSource = PlopMessageSource(plugin)
        messageSource.load()
        messageService = MessageService(plugin)

        // Load configs initially to get all necessary data
        plugin.slF4JLogger.info("Initiating config manager")
        configManager = ConfigReader(plugin)
        configManager.load()
        plugin.slF4JLogger.info("Finished loading config manager")

        DB = HikariManager(plugin)
        DB.load()

        // Get instance of hooks
        plugin.slF4JLogger.info("Hooking into other plugins")
        hooks = Hooks(plugin)
        hooks.load()

        // Load the two parts of the plugin
        plugin.slF4JLogger.info("Initiating plots")
        plots = Plots(plugin)
        plots.load()
        plugin.slF4JLogger.info("Initiating shops")
        shops = Shops(plugin)
        shops.load()



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
        previewListener = PreviewListener(this)
        mythicListener = MythicListener(this)
        playerInteractListener = PlayerInteract(this)

        // Listeners
        listOf(
            generalListener,
            mythicListener,
            previewListener,
            playerInteractListener
        ).forEach { listener -> server.pluginManager.registerSuspendingEvents(listener, this) }
        plugin.slF4JLogger.info("Finished hooking listeners")
    }

    private fun loadCmds() {
        commandManager = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(plugin)

        annotationParser = AnnotationParser(commandManager, CommandSourceStack::class.java)
        // The plugin will be using coroutines extensively for scheduling
        annotationParser.installCoroutineSupport()

        annotationParser.parse(GeneralSuggestionProcessor(plugin))

        commandManager.parserRegistry().registerParser(PlotTypeParser.plotTypeParser())
        commandManager.parserRegistry().registerParser(PlayerParser.playerParser())

        listOf(
            CommandAdminUnclaimPlot(plugin),
            CommandAdminResetPlot(plugin),
            CommandPlotHome(plugin),
            CommandPlotVisit(plugin),
            CommandPlotSetEntrance(plugin),
            CommandPlotSetHome(plugin),
            CommandPlotVisitToggle(plugin),
            CommandPlotPreview(plugin),

        ).forEach { command ->
            logger.info("Registering command: ${command.javaClass.simpleName}")
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
        val packetEvents = PacketEvents(plugin)
        val mythicMobs = MythicMobs(plugin)
        val worldGuard = WorldGuard(plugin)
        val economy = Economy(plugin)

        override suspend fun load() {
            listOf(
                guilds,
                mythicMobs,
                worldGuard,
                economy
            ).forEach { hook -> hook.load() }
            com.github.retrooper.packetevents.PacketEvents.getAPI().init()
            com.github.retrooper.packetevents.PacketEvents.getAPI().eventManager.registerListener(packetEvents, PacketListenerPriority.NORMAL)
        }

        override suspend fun kill() {
            listOf(
                guilds,
                mythicMobs,
                worldGuard,
                economy
            ).forEach { hook -> hook.kill() }
        }
    }

    class Menus(val plugin: Plop) {
        // Selectors
        val selectionSelfMenu = SelectionSelfMenu(plugin)
        val selectionOtherMenu = SelectionOtherMenu(plugin)

        // Hotbar
        val hotbarPreview = HotbarPreview(plugin)

        // Nexus configs
        val nexusMainMenu = MenuNexusMain(plugin)
        val nexusLogsMenu = MenuPlotLogs(plugin)
        val nexusTotemsMenu = MenuTotemList(plugin)
        val nexusUpgradeMenu = MenuUpgrade(plugin)

        // Plot Visit
        val plotVisitMenu = MenuPlotList(plugin)
    }

    val locationUtils = LocationUtils(plugin)
}