package com.brinkmc.plop

import com.brinkmc.plop.factory.Factories
import com.brinkmc.plop.plot.Plots
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.command.admin.*
import com.brinkmc.plop.shared.command.plot.general.CommandNexusBook
import com.brinkmc.plop.shared.command.plot.general.CommandPlotHome
import com.brinkmc.plop.shared.command.plot.general.CommandPlotVisit
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetEntrance
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotSetHome
import com.brinkmc.plop.shared.command.plot.nexus.CommandPlotVisitToggle
import com.brinkmc.plop.shared.command.plot.preview.CommandPlotPreview
import com.brinkmc.plop.shared.command.processors.GeneralSuggestionProcessor
import com.brinkmc.plop.shared.command.utils.PlotTypeParser
import com.brinkmc.plop.shared.config.ConfigHandler
import com.brinkmc.plop.shared.hook.listener.GeneralListener
import com.brinkmc.plop.shared.hook.listener.GuildListener
import com.brinkmc.plop.shared.hook.listener.PreviewListener
import com.brinkmc.plop.shared.hook.listener.DamageListener
import com.brinkmc.plop.shared.hook.listener.NexusListener
import com.brinkmc.plop.shared.hook.api.PlayerTracker
import com.brinkmc.plop.shared.hook.listener.ShopListener
import com.brinkmc.plop.shared.hook.listener.TotemListener
import com.brinkmc.plop.shared.hook.listener.VisitListener
import com.brinkmc.plop.shared.db.HikariManager
import com.brinkmc.plop.shared.gui.MenuHandler
import com.brinkmc.plop.shared.hologram.HologramHandler
import com.brinkmc.plop.shared.hook.HookHandler
import com.brinkmc.plop.shared.util.ClaimUtils
import com.brinkmc.plop.shared.util.LocationString
import com.brinkmc.plop.shared.util.design.DesignHandler
import com.brinkmc.plop.shared.util.design.MessageSource
import com.brinkmc.plop.shop.Shops
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.google.gson.Gson
import com.noxcrew.interfaces.InterfacesListeners
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.Audiences
import org.bukkit.NamespacedKey
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import java.io.File


class Plop : State, SuspendingJavaPlugin() {

    private val plugin = this
    lateinit var DB: HikariManager
    lateinit var gson: Gson

    // 3 main modules of the plugin
    lateinit var plots: Plots
    lateinit var shops: Shops
    lateinit var factories: Factories

    // Shared modules
    lateinit var menuHandler: MenuHandler
    lateinit var configHandler: ConfigHandler
    lateinit var hookHandler: HookHandler
    lateinit var designHandler: DesignHandler
    lateinit var hologramHandler: HologramHandler
    lateinit var audiences: Audiences


    // Private components for plop use only
    private lateinit var commandManager: PaperCommandManager<CommandSourceStack>
    private lateinit var annotationParser: AnnotationParser<CommandSourceStack>
    private lateinit var messageSource: MessageSource



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
        messageSource = MessageSource(plugin)
        messageSource.load()
        designHandler = DesignHandler(plugin)

        // Load configs initially to get all necessary data
        plugin.slF4JLogger.info("Initiating config manager")
        configHandler = ConfigHandler(plugin)
        configHandler.load()
        plugin.slF4JLogger.info("Finished loading config manager")

        DB = HikariManager(plugin)
        DB.load()

        // Get instance of hooks
        plugin.slF4JLogger.info("Hooking into other plugins")
        hookHandler = HookHandler(plugin)
        hookHandler.load()

        // Load the two parts of the plugin
        plugin.slF4JLogger.info("Initiating plots")
        plots = Plots(plugin)
        plots.load()
        plugin.slF4JLogger.info("Initiating shops")
        shops = Shops(plugin)
        shops.load()
        plugin.slF4JLogger.info("Initiating factories")
        factories = Factories(plugin)
        factories.load()

        // Load displays
        plugin.slF4JLogger.info("Initiating displays")
        hologramHandler = HologramHandler(plugin)
        hologramHandler.load()

        // Enable all menus
        plugin.slF4JLogger.info("Creating menus and hotbars")
        menuHandler = MenuHandler(plugin)

        // Register listener

        // Finally enable commands
        loadCmds()
    }

    override suspend fun kill() {
        plots.kill()
        shops.kill()
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
            CommandNexusBook(plugin),
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

    fun getFile(fileName: String): File {
        return File(plugin.dataFolder, fileName)
    }

    fun plopMessageSource(): MessageSource {
        return this.messageSource
    }

    val namespacedKey: NamespacedKey
        get() = NamespacedKey(plugin, "plop")

    suspend fun <T> syncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.minecraftDispatcher, block)

    suspend fun <T> asyncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.asyncDispatcher, block)
}