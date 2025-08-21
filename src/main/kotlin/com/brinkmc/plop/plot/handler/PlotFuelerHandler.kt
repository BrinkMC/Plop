package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level

class PlotFuelerHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        plotConfig.getFuelerLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild fueler levels
        plotConfig.getFuelerLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }


        // Start redstone timer per plot to update fueler amount
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    fun getHighestLevel(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.size
            PlotType.PERSONAL -> personalLevels.size
        }
    }

    fun getLevel(plotType: PlotType, level: Int): Level? {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.getOrNull(level)
            PlotType.PERSONAL -> personalLevels.getOrNull(level)
        }
    }

    fun getCurrentFuelerLimit(plotType: PlotType, level: Int): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getMaximumFuelerLimit(plotType: PlotType) : Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value ?: -1
            PlotType.PERSONAL -> personalLevels.last().value ?: -1
        }
    }

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.fueler.level += 1
    }
}