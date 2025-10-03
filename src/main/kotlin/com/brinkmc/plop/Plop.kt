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
import com.brinkmc.plop.shared.db.HikariManager
import com.brinkmc.plop.shared.design.MessageSource
import com.brinkmc.plop.shared.hologram.HologramHandler
import com.brinkmc.plop.shared.item.ItemService
import com.brinkmc.plop.shared.service.*
import com.brinkmc.plop.shop.Shops
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.google.gson.Gson
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.LivingEntity
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import java.io.File
import java.util.*


class Plop : State, SuspendingJavaPlugin() {

    private val plugin = this
    lateinit var DB: HikariManager
    lateinit var gson: Gson

    // 3 main modules of the plugin
    val plots: Plots = Plots(plugin)
    val shops: Shops = Shops(plugin)
    val factories: Factories = Factories(plugin)

    // Shared services

    val menuService: MenuService = MenuService(plugin)
    val hologramService: HologramService = HologramService(plugin)
    val configService: ConfigService = ConfigService(plugin)
    val hookService: HookService = HookService(plugin)
    val designService: DesignService = DesignService(plugin)
    val playerService: PlayerService = PlayerService(plugin)
    val economyService: EconomyService = EconomyService(plugin)
    val itemService: ItemService = ItemService(plugin)

    // Private components for plop use only
    private lateinit var commandManager: PaperCommandManager<CommandSourceStack>
    private lateinit var annotationParser: AnnotationParser<CommandSourceStack>
    private val messageSource: MessageSource = MessageSource(plugin)



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
        messageSource.load()
        playerService.load()

        // Load configs initially to get all necessary data
        plugin.slF4JLogger.info("Initiating config manager")
        configService.load()
        plugin.slF4JLogger.info("Finished loading config manager")

        DB = HikariManager(plugin)
        DB.load()

        // Get instance of hooks
        plugin.slF4JLogger.info("Hooking into other plugins")
        hookService.load()

        // Load the two parts of the plugin
        plugin.slF4JLogger.info("Initiating plots")
        plots.load()
        plugin.slF4JLogger.info("Initiating shops")
        shops.load()
        plugin.slF4JLogger.info("Initiating factories")
        factories.load()

        // Load displays
        plugin.slF4JLogger.info("Initiating displays")
        hologramService.load()

        // Enable all menus
        plugin.slF4JLogger.info("Creating menus and hotbars")
        menuService.load()

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

    fun getFirstLivingEntity(): LivingEntity? {
        for (world in Bukkit.getWorlds()) {
            for (entity in world.livingEntities) {
                return entity
            }
        }
        return null
    }

    fun plopMessageSource(): MessageSource {
        return this.messageSource
    }

    fun consoleCommand(command: String, playerId: UUID) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
            command
                .replace("{player_uuid}", playerId.toString())
                .replace("{player_name}", playerService.getUsername(playerId) ?: "")
        )
    }

    suspend fun <T> syncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.minecraftDispatcher, block)

    suspend fun <T> asyncScope(block: suspend CoroutineScope.() -> T): T = withContext(plugin.asyncDispatcher, block)
}