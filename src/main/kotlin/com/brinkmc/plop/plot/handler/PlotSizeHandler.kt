package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level

class PlotSizeHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        plotConfig.getPlotSizeLevels(PlotType.GUILD)?.let { guildLevels.addAll(it) } // Add all guild plot size levels
        plotConfig.getPlotSizeLevels(PlotType.PERSONAL)?.let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    fun getHighestLevel(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.size - 1
            PlotType.PERSONAL -> personalLevels.size - 1
        }
    }

    fun getLevel(plotType: PlotType, toFind: Int): Level {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[toFind]
            PlotType.PERSONAL -> personalLevels[toFind]
        }
    }

    fun getCurrentPlotSize(plotType: PlotType, level: Int): Int { // Getter
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getMaximumPlotSize(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> plotConfig.getPlotSizeLevels(PlotType.GUILD)?.last()?.value ?: -1 // Last gets largest item
            PlotType.PERSONAL -> plotConfig.getPlotSizeLevels(PlotType.PERSONAL)?.last()?.value ?: -1
        }
    }

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.size.level += 1
    }
}