package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.PermissionKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import org.bukkit.entity.Player
import java.security.Permission
import java.util.UUID

class PlotUpgradeService(override val plugin: Plop): Addon, State {
    override suspend fun load() {
        logger.info("Loaded PlotUpgradeHandler")

    }

    override suspend fun kill() { }


    suspend fun upgradeSizeLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
        logger.info("Attempting to upgrading plot claim for $plotId by $playerId")

        // Is player the owner of the plot?
        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }


        // Has permission to upgrade
        if (!playerService.hasPermission(playerId, PermissionKey.UPGRADE_PLOT)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE)
        }

        val currentLevel = plotSizeService.getPlotSizeLevel(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        if (!plotSizeService.canUpgradePlotSize(plotId)) {
            return ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
        }

        plotService.get

        // Calculate if they can afford it
        val potentialLevel =  plots.sizeHandler.getLevel(plot.type, plot.size.level + 1)

        // Using the economy API, check if they can afford it

        if (!plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.sizeHandler.upgradePlot(plot) // Update the plot in the database
    }
}