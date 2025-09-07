package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level

class PlotFactoryService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        plotConfig.getFactoryLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        plotConfig.getFactoryLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    fun getHighestLevel(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> plotConfig.getFactoryLevels(PlotType.GUILD).last().value ?: -1 // Last gets largest item
            PlotType.PERSONAL -> plotConfig.getFactoryLevels(PlotType.PERSONAL).last().value ?: -1
        }
    }

    fun getLevel(plotType: PlotType, level: Int): Level { // Get current limit based on level
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level]
            PlotType.PERSONAL -> personalLevels[level]
        }
    }

    fun getCurrentFactoryLimit(plotType: PlotType, level: Int): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getMaximumFactoryLimit(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value ?: -1
            PlotType.PERSONAL -> personalLevels.last().value ?: -1
        }
    }

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.factory.level += 1
    }
}