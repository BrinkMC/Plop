package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.dto.structure.TotemType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level

class PlotTotemService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        plotConfig.getTotemLimit(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        plotConfig.getTotemLimit(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    fun getTotemTypeFromKey(key: String): TotemType? {
        var string = key
        if (key.startsWith(totemConfig.totemId)) {
            string = string.substringAfter(totemConfig.totemId)
        }

        TotemType.entries.find {
            it.name == string
        }?.let {
            return it
        }
        return null
    }

    fun getHighestLevel(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.size
            PlotType.PERSONAL -> personalLevels.size
        }
    }

    fun getLevel(plotType: PlotType, level: Int): Level {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level]
            PlotType.PERSONAL -> personalLevels[level]
        }
    }

    fun getCurrentTotemLimit(plotType: PlotType, level: Int): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getMaximumTotemLimit(plotType: PlotType) : Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value ?: -1
            PlotType.PERSONAL -> personalLevels.last().value ?: -1
        }
    }

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.shop.level += 1
    }
}