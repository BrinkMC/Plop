package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotFactory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import java.util.UUID

class PlotFactoryService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getFactoryLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getFactoryLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    private suspend fun getPlotFactory(plotId: UUID) = plotService.getPlotFactory(plotId)

    suspend fun checkFactoryLimit(plotId: UUID): Boolean {
        val factoryLimit = getFactoryLimit(plotId) ?: return false
        val factoryCount = getPlotFactoryCount(plotId)
        return factoryCount < factoryLimit // Return true if under the limit
    }

    suspend fun getFactoryLimit(plotId: UUID): Int? {
        val plotFactory = getPlotFactory(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> guildLevels[plotFactory.level].value
            PlotType.PERSONAL -> personalLevels[plotFactory.level].value
        }
    }

    suspend fun getMaximumFactoryLimit(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null

        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    suspend fun getPlotFactoryCount(plotId: UUID) = factoryService.getFactoryCount(plotId)

    suspend fun getPlotFactoryLevel(plotId: UUID): Int? {
        return getPlotFactory(plotId)?.level
    }

    suspend fun canUpgradePlotFactory(plotId: UUID): Boolean {
        val plotType = plotService.getPlotType(plotId) ?: return false
        val plotFactoryLevel = getPlotFactoryLevel(plotId) ?: return false
        return when(plotType) {
            PlotType.GUILD -> plotFactoryLevel < guildLevels.size - 1
            PlotType.PERSONAL -> plotFactoryLevel < personalLevels.size - 1
        }
    }

    suspend fun getCostOfUpgrade(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotFactoryLevel = getPlotFactoryLevel(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> {
                if (!canUpgradePlotFactory(plotId)) return null
                guildLevels[plotFactoryLevel + 1].price
            }
            PlotType.PERSONAL -> {
                if (!canUpgradePlotFactory(plotId)) return null
                personalLevels[plotFactoryLevel + 1].price
            }
        }
    }

    // Setters

    private suspend fun setPlotFactoryLevel(plotId: UUID, level: Int) {
        val plotFactory = getPlotFactory(plotId) ?: return
        plotFactory.setLevel(level)
    }


    suspend fun upgradePlotFactory(plotId: UUID): ServiceResult {
        val plotType = plotService.getPlotType(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val plotFactory = getPlotFactory(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        return when(plotType) {
            PlotType.GUILD if(canUpgradePlotFactory(plotId)) -> {
                setPlotFactoryLevel(plotId, plotFactory.level + 1)
                ServiceResult.Failure(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            PlotType.PERSONAL if (canUpgradePlotFactory(plotId)) -> {

                setPlotFactoryLevel(plotId, plotFactory.level + 1)
                ServiceResult.Failure(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            else -> {
                ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
            }
        }
    }
}