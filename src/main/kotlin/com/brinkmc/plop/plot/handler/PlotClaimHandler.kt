package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.data.Claim
import com.brinkmc.plop.plot.plot.data.PlotVisit
import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.LocationUtils.getSafeDestination
import java.util.UUID

class PlotClaimHandler(override val plugin: Plop): Addon, State {
    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun initiateClaim(player: UUID, plotType: PlotType) {
        val previewInstance = plots.plotPreviewHandler.previews[player] ?: return

        val newPlot = Plot(
            UUID.randomUUID(),
            plotType,
            player,
            Claim(
                previewInstance.viewPlot.value.world,
                previewInstance.viewPlot.value.toLocation(),
                previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation(),
                previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation()
            ),

            VisitorLimit(1, plotConfig.getVisitorLimit(plotType)[0]),
            PlotSize(1),
            FactoryLimit(1),
            ShopLimit(1),
            listOf(), // No totems for brand new plot
            PlotVisit(0, 0, 0)

        )

        plugin.hooks.worldGuard.createRegion()
        TODO("Integrate with Guilds, Integrate with WorldGuard")

        plots.plotPreviewHandler.claimPlot(player)
    }
}