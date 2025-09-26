package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.dto.structure.TotemType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
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

    suspend fun getTotemLimit(plotId: UUID): Int? {
        val plotTotem = plotService.getPlotTotem(plotId) ?: return null
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

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.shop.level += 1
    }
}