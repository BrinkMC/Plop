package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.handler.PlotClaimHandler
import com.brinkmc.plop.plot.handler.PlotFactoryHandler
import com.brinkmc.plop.plot.handler.PlotHandler
import com.brinkmc.plop.plot.handler.PlotPreviewHandler
import com.brinkmc.plop.plot.handler.PlotShopHandler
import com.brinkmc.plop.plot.handler.PlotSizeHandler
import com.brinkmc.plop.plot.handler.PlotUpgradeHandler
import com.brinkmc.plop.plot.handler.PlotVisitorHandler
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.data.PlotVisit
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.gui.preview.HotbarPreview
import com.brinkmc.plop.shared.storage.PlotKey
import org.bukkit.entity.Player
import java.util.UUID

class Plots(override val plugin: Plop): Addon, State {

    val plotMap: HashMap<PlotKey, Plot> = hashMapOf()

    // Sub handlers
    lateinit var plotHandler: PlotHandler
    lateinit var plotVisitorHandler: PlotVisitorHandler
    lateinit var plotUpgradeHandler: PlotUpgradeHandler
    lateinit var plotPreviewHandler: PlotPreviewHandler
    lateinit var plotFactoryHandler: PlotFactoryHandler
    lateinit var plotClaimHandler: PlotClaimHandler
    lateinit var plotShopHandler: PlotShopHandler
    lateinit var plotSizeHandler: PlotSizeHandler

    val hotbarPreview = HotbarPreview(plugin)

    override fun load() {
        plotHandler = PlotHandler(plugin)
        plotVisitorHandler = PlotVisitorHandler(plugin)
        plotUpgradeHandler = PlotUpgradeHandler(plugin)
        plotPreviewHandler = PlotPreviewHandler(plugin)
        plotFactoryHandler = PlotFactoryHandler(plugin)
        plotClaimHandler = PlotClaimHandler(plugin)
        plotShopHandler = PlotShopHandler(plugin)
        plotSizeHandler = PlotSizeHandler(plugin)

        listOf(
            plotHandler,
            plotVisitorHandler,
            plotUpgradeHandler,
            plotPreviewHandler,
            plotFactoryHandler,
            plotClaimHandler,
            plotShopHandler,
            plotSizeHandler
        ).forEach { handler -> (handler as State).load() }
    }

    override fun kill() {
        TODO("Not yet implemented")
    }

    fun getPlot(plotId: UUID): Plot? {
        return plotHandler.getPlotById(plotId)
    }

    // Extension functions
    fun Player.personalPlot(): Plot? {
        // Get a list of all the plots player owns. 1-to-1 relationship
        return plotHandler.getPlotByOwner(uniqueId)

    }

    fun Player.guildPlot(): Plot? {
        // One-liner to return only guild plots, which have the player as a member or the player as the leader
        return plotHandler.getPlotByGuild(player.guild().uuid)
    }
}