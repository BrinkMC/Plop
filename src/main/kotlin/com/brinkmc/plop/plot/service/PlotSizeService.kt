package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
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

    suspend fun getPlotSize(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null
        val plotSize = plotService.getPlotSize(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotSize.level].value
            PlotType.PERSONAL -> personalLevels[plotSize.level].value
        }
    }
}