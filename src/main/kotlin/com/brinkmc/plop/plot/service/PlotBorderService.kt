package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.BukkitUtils
import com.brinkmc.plop.shared.util.BukkitUtils.player
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import org.bukkit.Bukkit
import java.util.UUID

class PlotBorderService(override val plugin: Plop): State, Addon {



    override suspend fun load() {

    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun updateBorder(playerId: UUID) { plugin.async {
        // Update player tracker for handy information

        if (plotPreviewService.isInPreview(playerId)) { // Check if player is in a preview
            val previewType = plotPreviewService.getPlotType(playerId) ?: return@async // Make sure preview type isn't null
            val viewPlot = plotPreviewService.getPreviewPlot(playerId) ?: return@async // Make sure preview plot isn't null

            playerService.setBorder(playerId, previewType, viewPlot, true)
            return@async
        }

        // PlayerService, ergo the plot they are CURRENTLY in
        val plotId = playerService.getPlotId(playerId) ?: run {
            playerService.resetBorder(playerId)
            return@async
        }

        val plotType = plotService.getPlotType(plotId) ?: return@async // Make sure plot type isn't null
        val plotCentre = plotClaimService.getPlotCentre(plotId) ?: return@async // Make sure plot centre isn't null
        val plotSize = plotSizeService.getPlotSize(plotId) ?: return@async // Make sure plot size isn't null

        playerService.setBorder(playerId, plotType, plotCentre, false, plotSize)
        return@async
    } }
}
