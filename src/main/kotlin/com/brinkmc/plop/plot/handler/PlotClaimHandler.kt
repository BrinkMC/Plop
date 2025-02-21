package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.PlotClaim
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import java.util.UUID

class PlotClaimHandler(override val plugin: Plop): Addon, State {
    override suspend fun load() { }

    override suspend fun kill() { }

    suspend fun initiateClaim(player: UUID, plotType: PlotType) {
        val previewInstance = plots.previewHandler.getPreview(player)
        
        if (previewInstance == null) {
            player.player()?.sendMiniMessage(lang.get("preview.start.no-preview"))
            return
        }

        val plotClaim = PlotClaim(
            previewInstance.viewPlot.value.toLocation(),
            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation(),
            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation()
        )

        val uuid = when (plotType) { // Determine plot ID based on plot type
            PlotType.PERSONAL -> {
                player
            }
            PlotType.GUILD -> {
                player.guild()?.id ?: return
            }
        }

        val newPlot = Plot(
            uuid,
            plotType,
            plotClaim,
            PlotVisit(true, 0, 0, mutableListOf(), plotType),
            PlotSize(0, plotType),
            PlotFactory(0, mutableListOf(), plotType),
            PlotShop(0, mutableListOf(), plotType),
            PlotTotem(0, mutableListOf(), plotType) // No totems for a brand-new plot
        )

        plots.handler.addPlot(newPlot) // Register new plot in handler

        plots.previewHandler.claimPlot(player) // Finalise preview

        plugin.hooks.worldGuard.createRegion(uuid) // Create region in WorldGuard

        // Send in the schematic
        val schematic = plots.nexusManager.getSchematic()

        player.player()?.updateBorder() // Update the border again to new smaller size
    }
}