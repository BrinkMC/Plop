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
import com.brinkmc.plop.shared.command.shop.CommandShopList
import com.brinkmc.plop.shared.command.shop.CommandTrade
import com.brinkmc.plop.shared.util.MessageService
import com.brinkmc.plop.shared.util.PlopMessageSource
import com.brinkmc.plop.shop.Shops
import com.google.gson.Gson
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.checkerframework.checker.units.qual.C
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.bukkit.CloudBukkitCapabilities
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.meta.CommandMeta
import org.incendo.cloud.meta.SimpleCommandMeta
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import java.io.File


class Plop : State,JavaPlugin() {
    private val plugin = this

    lateinit var plots: Plots
    lateinit var shops: Shops

    private lateinit var commandManager: PaperCommandManager<Source>
    private lateinit var annotationParser: AnnotationParser<Source>
    private lateinit var messageSource: PlopMessageSource
    private lateinit var messageService: MessageService

    lateinit var messaging: Gson

    override fun onEnable() {
        load()
    }

    override fun onDisable() {

    }

    /*
    Putting everything inside a load function results in easy reload of the entire plugin if necessary based off of the state system
     */
    override fun load() {
        // Load the two parts of the plugin
        plots = Plots(plugin)
        shops = Shops(plugin)

        // Enable gson library for messages

        this.messaging = Gson()

        // Finally enable commands
        loadCmds()
    }

    override fun kill() {
        plots.kill()
        shops.kill()
    }

    private fun loadCmds() {
        commandManager = PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(plugin)

        annotationParser = AnnotationParser(commandManager, Source::class.java)
        // The plugin will be using coroutines extensively for scheduling
        annotationParser.installCoroutineSupport()

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
        )
            .forEach { command -> annotationParser.parse(command) }
    }

    fun getFile(fileName: String): File? {
        return plugin.dataFolder.listFiles()?.find { it.name == fileName }
    }

    fun messageService(): MessageService {
        return this.messageService
    }

    fun plopMessageSource(): PlopMessageSource {
        return this.messageSource
    }
}