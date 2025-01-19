package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.plotType
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class PlotSizeHandler(override val plugin: Plop): Addon, State {

    val guildLevels = mutableListOf<Int>()
    val personalLevels = mutableListOf<Int>()

    override suspend fun load() {
        guildLevels.addAll(plotConfig.guildPlotSizeLevels) // Add all guild plot size levels
        personalLevels.addAll(plotConfig.personalPlotSizeLevels)
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    fun getCurrentPlotSize(plot: Plot): Int {
        return when (plot.type) {
            plotType.GUILD -> guildLevels[plot.plotSize.level]
            plotType.PERSONAL -> personalLevels[plot.plotSize.level]
        }
    }

    fun getPlotSizeLimit(plot: Plot): Int {
        return when (plot.type) {
            plotType.GUILD -> plotConfig.guildPlotSizeLevels.size
            plotType.PERSONAL -> plotConfig.personalPlotSizeLevels.size
        }
    }
}