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


    suspend fun upgradePlotSizeLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
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

        val price = plotSizeService.getCostOfUpgrade(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val balance = economyService.getBalance(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        // Calculate if they can afford it
        if (balance < price) {
            return ServiceResult.Failure(MessageKey.NO_MONEY, SoundKey.FAILURE)
        }

        return plotSizeService.upgradePlotSize(plotId) // Update the plot in the database
    }

    suspend fun upgradePlotFactoryLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
        logger.info("Attempting to upgrading plot factory for $plotId by $playerId")

        // Is player the owner of the plot?
        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }


        // Has permission to upgrade
        if (!playerService.hasPermission(playerId, PermissionKey.UPGRADE_PLOT)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE)
        }

        val currentLevel = plotFactoryService.getPlotFactoryLevel(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        if (!plotFactoryService.canUpgradePlotFactory(plotId)) {
            return ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
        }

        val price = plotFactoryService.getCostOfUpgrade(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val balance = economyService.getBalance(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        // Calculate if they can afford it
        if (balance < price) {
            return ServiceResult.Failure(MessageKey.NO_MONEY, SoundKey.FAILURE)
        }

        return plotFactoryService.upgradePlotFactory(plotId) // Update the plot in the database
    }

    suspend fun upgradePlotShopLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
        logger.info("Attempting to upgrading plot shop for $plotId by $playerId")

        // Is player the owner of the plot?
        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }


        // Has permission to upgrade
        if (!playerService.hasPermission(playerId, PermissionKey.UPGRADE_PLOT)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE)
        }

        val currentLevel = plotShopService.getPlotShopLevel(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        if (!plotShopService.canUpgradePlotShop(plotId)) {
            return ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
        }

        val price = plotShopService.getCostOfUpgrade(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val balance = economyService.getBalance(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        // Calculate if they can afford it
        if (balance < price) {
            return ServiceResult.Failure(MessageKey.NO_MONEY, SoundKey.FAILURE)
        }

        return plotShopService.upgradePlotShop(plotId) // Update the plot in the database
    }

    suspend fun upgradePlotVisitLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
        logger.info("Attempting to upgrading plot visit for $plotId by $playerId")

        // Is player the owner of the plot?
        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }


        // Has permission to upgrade
        if (!playerService.hasPermission(playerId, PermissionKey.UPGRADE_PLOT)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE)
        }

        val currentLevel = plotVisitService.getPlotVisitLevel(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        if (!plotVisitService.canUpgradePlotVisit(plotId)) {
            return ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
        }

        val price = plotVisitService.getCostOfUpgrade(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val balance = economyService.getBalance(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        // Calculate if they can afford it
        if (balance < price) {
            return ServiceResult.Failure(MessageKey.NO_MONEY, SoundKey.FAILURE)
        }

        return plotVisitService.upgradePlotVisit(plotId) // Update the plot in the database
    }

    suspend fun upgradePlotTotemLevel(plotId: UUID, playerId: UUID): ServiceResult { // Validate that they can afford price of new level
        logger.info("Attempting to upgrading plot totem for $plotId by $playerId")

        // Is player the owner of the plot?
        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE)
        }


        // Has permission to upgrade
        if (!playerService.hasPermission(playerId, PermissionKey.UPGRADE_PLOT)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE)
        }

        val currentLevel = plotTotemService.getPlotTotemLevel(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        if (!plotTotemService.canUpgradeTotemLimit(plotId)) {
            return ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
        }

        val price = plotTotemService.getCostOfUpgrade(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val balance = economyService.getBalance(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)

        // Calculate if they can afford it
        if (balance < price) {
            return ServiceResult.Failure(MessageKey.NO_MONEY, SoundKey.FAILURE)
        }

        return plotTotemService.upgradePlotTotem(plotId) // Update the plot in the database
    }
}