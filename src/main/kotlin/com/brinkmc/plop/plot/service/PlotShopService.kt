package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotShop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import java.util.UUID

class PlotShopService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getShopLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getShopLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    suspend fun checkShopLimit(plotId: UUID): Boolean {
        val shopLimit = getShopLimit(plotId) ?: return false
        val shopCount = getPlotShopCount(plotId)
        return shopCount < shopLimit // Return true if under the limit
    }

    private suspend fun getPlotShop(plotId: UUID): PlotShop? = plotService.getPlotShop(plotId)

    suspend fun getPlotShopCount(plotId: UUID) = shopService.getShopCount(plotId)


    suspend fun getShopLimit(plotId: UUID): Int? {
        val plotShop = getPlotShop(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotShop.level].value
            PlotType.PERSONAL -> personalLevels[plotShop.level].value
        }
    }

    suspend fun getMaximumShopLimit(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null

        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    suspend fun getPlotShopLevel(plotId: UUID): Int? {
        return getPlotShop(plotId)?.level
    }

    suspend fun canUpgradePlotShop(plotId: UUID): Boolean {
        val plotType = plotService.getPlotType(plotId) ?: return false
        val plotShop = getPlotShop(plotId) ?: return false
        return when(plotType) {
            PlotType.GUILD -> plotShop.level < guildLevels.size - 1
            PlotType.PERSONAL -> plotShop.level < personalLevels.size - 1
        }
    }

    suspend fun getCostOfUpgrade(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotShopLevel = getPlotShopLevel(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> {
                if (!canUpgradePlotShop(plotId)) return null
                guildLevels[plotShopLevel + 1].price
            }
            PlotType.PERSONAL -> {
                if (!canUpgradePlotShop(plotId)) return null
                personalLevels[plotShopLevel + 1].price
            }
        }
    }

    // Setters

    private suspend fun setPlotShopLevel(plotId: UUID, level: Int) {
        val plotShop = getPlotShop(plotId) ?: return
        plotShop.setLevel(level)
    }


    suspend fun upgradePlotShop(plotId: UUID): ServiceResult {
        val plotType = plotService.getPlotType(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val plotShop = getPlotShop(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        return when(plotType) {
            PlotType.GUILD if(canUpgradePlotShop(plotId)) -> {
                setPlotShopLevel(plotId, plotShop.level + 1)
                ServiceResult.Failure(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            PlotType.PERSONAL if (canUpgradePlotShop(plotId)) -> {

                setPlotShopLevel(plotId, plotShop.level + 1)
                ServiceResult.Failure(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            else -> {
                ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
            }
        }
    }
}