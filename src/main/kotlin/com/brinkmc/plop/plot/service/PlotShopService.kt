package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import java.util.UUID

class PlotShopService(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        configService.plotConfig.getShopLevels(PlotType.GUILD).let { guildLevels.addAll(it) } // Add all guild plot size levels
        configService.plotConfig.getShopLevels(PlotType.PERSONAL).let { personalLevels.addAll(it) }
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

    fun getShopLimit(plotId: UUID): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getShopCount(plotId: UUID): Int {
        shopService.
    }

    fun getMaximumShopLimit(plotType: PlotType) : Int {
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