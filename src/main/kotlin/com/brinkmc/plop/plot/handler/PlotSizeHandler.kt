package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class PlotSizeHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Int>()
    private val personalLevels = mutableListOf<Int>()

    override suspend fun load() {
        guildLevels.addAll(plotConfig.getPlotSizeLevels(PlotType.GUILD)) // Add all guild plot size levels
        personalLevels.addAll(plotConfig.getPlotSizeLevels(PlotType.PERSONAL))
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    fun getCurrentPlotSize(plot: Plot): Int { // Getter
        return when (plot.type) {
            PlotType.GUILD -> guildLevels[plot.size.level]
            PlotType.PERSONAL -> personalLevels[plot.size.level]
        }
    }

    fun getPlotSizeLimit(plot: Plot): Int {
        return when (plot.type) {
            PlotType.GUILD -> plotConfig.getPlotSizeLevels(PlotType.GUILD).size
            PlotType.PERSONAL -> plotConfig.getPlotSizeLevels(PlotType.PERSONAL).size
        }
    }
}