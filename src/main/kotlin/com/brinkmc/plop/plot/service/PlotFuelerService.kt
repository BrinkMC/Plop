package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotFueler
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import java.util.UUID
import kotlin.text.get

class PlotFuelerService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getFuelerLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild fueler levels
        configService.plotConfig.getFuelerLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }


        // Start redstone timer per plot to update fueler amount
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    private suspend fun getPlotFueler(plotId: UUID): PlotFueler? {
        val plotFueler = plotService.getPlotFueler(plotId) ?: return null
        return plotFueler
    }

    suspend fun getFuelerLevel(plotId: UUID): Int? {
        val plotFueler = getPlotFueler(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> guildLevels[plotFueler.level].value
            PlotType.PERSONAL -> personalLevels[plotFueler.level].value
        }
    }

    suspend fun getMaximumFuelerLevel(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null

        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    // Setters

    suspend fun upgradeFuelerLimit(plotId: UUID) {
        val plotFueler = getPlotFueler(plotId) ?: return
        val plotType = plotService.getPlotType(plotId) ?: return

        plotFueler.setLevel(plotFueler.level + 1)
    }
}