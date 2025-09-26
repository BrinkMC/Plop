package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotVisit
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import java.util.UUID

class PlotVisitService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getVisitorLimit(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getVisitorLimit(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear()
        personalLevels.clear()
    }

    // Getters

    private suspend fun getPlotVisit(plotId: UUID) = plotService.getPlotVisit(plotId)

    suspend fun getMaximumVisitors(plotId: UUID): Int? {
        val plotVisit = getPlotVisit(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotVisit.level].value
            PlotType.PERSONAL -> personalLevels[plotVisit.level].value
        }
    }

    // Setters

    fun upgradePlot(plot: Plot) {
        plot.visit.level += 1
    }
}