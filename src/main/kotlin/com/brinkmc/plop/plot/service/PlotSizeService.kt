package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotSize
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import java.util.UUID

class PlotSizeService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getPlotSizeLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getPlotSizeLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    private suspend fun getPlotSize(plotId: UUID): PlotSize? = plotService.getPlotSize(plotId)

    suspend fun getPlotSizeSize(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotSize = getPlotSize(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotSize.level].value
            PlotType.PERSONAL -> personalLevels[plotSize.level].value
        }
    }

    suspend fun getPlotSizeLevel(plotId: UUID): Int? {
        return getPlotSize(plotId)?.level
    }

    suspend fun canUpgradePlotSize(plotId: UUID): Boolean {
        val plotType = plotService.getPlotType(plotId) ?: return false
        val plotSize = getPlotSize(plotId) ?: return false
        return when(plotType) {
            PlotType.GUILD -> plotSize.level < guildLevels.size - 1
            PlotType.PERSONAL -> plotSize.level < personalLevels.size - 1
        }
    }

    suspend fun getCostOfUpgrade(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotSizeLevel = getPlotSizeLevel(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> {
                if (plotSizeLevel >= guildLevels.size - 1) return null
                guildLevels[plotSizeLevel + 1].price
            }
            PlotType.PERSONAL -> {
                if (plotSizeLevel >= personalLevels.size - 1) return null
                personalLevels[plotSizeLevel + 1].price
            }
        }
    }

    // Setters

    suspend fun up


}