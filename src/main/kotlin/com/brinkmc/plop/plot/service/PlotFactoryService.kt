package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotFactory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import java.util.UUID

class PlotFactoryService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getFactoryLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getFactoryLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    // Getters

    private suspend fun getPlotFactory(plotId: UUID) = plotService.getPlotFactory(plotId)

    suspend fun checkFactoryLimit(plotId: UUID): Boolean {
        val factoryLimit = getFactoryLimit(plotId) ?: return false
        val factoryCount = getPlotFactoryCount(plotId)
        return factoryCount < factoryLimit // Return true if under the limit
    }

    suspend fun getFactoryLimit(plotId: UUID): Int? {
        val plotFactory = getPlotFactory(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when (plotType) {
            PlotType.GUILD -> guildLevels[plotFactory.level].value
            PlotType.PERSONAL -> personalLevels[plotFactory.level].value
        }
    }

    suspend fun getMaximumFactoryLimit(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null

        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    suspend fun getPlotFactoryCount(plotId: UUID) = factoryService.getFactoryCount(plotId) 

    // Setters

    suspend fun upgradeFactoryLimit(plotId: UUID) {
        val plotFactory = getPlotFactory(plotId) ?: return
        val plotType = plotService.getPlotType(plotId) ?: return

        plotFactory.setLevel(plotFactory.level + 1)
    }


}