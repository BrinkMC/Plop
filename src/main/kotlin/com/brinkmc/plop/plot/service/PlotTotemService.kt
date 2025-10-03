package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import java.util.UUID

class PlotTotemService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getTotemLimit(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getTotemLimit(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    suspend fun checkTotemLimit(plotId: UUID): Boolean {
        val totemLimit = getTotemLimit(plotId) ?: return false
        val totemCount = getPlotTotemCount(plotId) ?: return false
        return totemCount < totemLimit
    }

    private suspend fun getPlotTotem(plotId: UUID) = plotService.getPlotTotem(plotId)

    suspend fun getPlotTotemCount(plotId: UUID): Int? {
        val plotTotem = getPlotTotem(plotId) ?: return null
        return plotTotem.totems.size
    }

    suspend fun getTotemLimit(plotId: UUID): Int? {
        val plotTotem = getPlotTotem(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> guildLevels[plotTotem.level].value
            PlotType.PERSONAL -> personalLevels[plotTotem.level].value
        }
    }

    suspend fun getMaximumTotemLimit(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    suspend fun getPlotTotemLevel(plotId: UUID): Int? {
        return getPlotTotem(plotId)?.level
    }

    suspend fun canUpgradeTotemLimit(plotId: UUID): Boolean {
        val plotType = plotService.getPlotType(plotId) ?: return false
        val plotTotem = getPlotTotem(plotId) ?: return false
        return when (plotType) {
            PlotType.GUILD -> plotTotem.level < guildLevels.size - 1
            PlotType.PERSONAL -> plotTotem.level < personalLevels.size - 1
        }
    }

    suspend fun getCostOfUpgrade(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotTotemLevel = getPlotTotemLevel(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> {
                if (!canUpgradeTotemLimit(plotId)) return null
                guildLevels[plotTotemLevel + 1].price
            }
            PlotType.PERSONAL -> {
                if (!canUpgradeTotemLimit(plotId)) return null
                personalLevels[plotTotemLevel + 1].price
            }
        }
    }

    // Setters

    private suspend fun setPlotTotemLevel(plotId: UUID, level: Int) {
        val plotTotem = getPlotTotem(plotId) ?: return
        plotTotem.setLevel(level)
    }

    suspend fun upgradePlotTotem(plotId: UUID): ServiceResult {
        val plotType = plotService.getPlotType(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        val plotTotem = getPlotTotem(plotId) ?: return ServiceResult.Failure(MessageKey.ERROR, SoundKey.FAILURE)
        return when(plotType) {
            PlotType.GUILD if(canUpgradeTotemLimit(plotId)) -> {
                setPlotTotemLevel(plotId, plotTotem.level + 1)
                ServiceResult.Success(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            PlotType.PERSONAL if (canUpgradeTotemLimit(plotId)) -> {

                setPlotTotemLevel(plotId, plotTotem.level + 1)
                ServiceResult.Success(MessageKey.UPGRADE_SUCCESS, SoundKey.SUCCESS)
            }
            else -> {
                ServiceResult.Failure(MessageKey.REACHED_MAX_UPGRADE_LEVEL, SoundKey.FAILURE)
            }
        }
    }

    suspend fun toggleLightening(plotId: UUID, enable: Boolean): Boolean {
        val plotTotem = getPlotTotem(plotId) ?: return false
        plotTotem.setEnableLightning(enable)
        return true
    }


}