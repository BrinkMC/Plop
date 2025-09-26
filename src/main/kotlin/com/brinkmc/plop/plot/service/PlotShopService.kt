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

    suspend fun checkShopLimit(plotId: UUID): Boolean {
        val shopLimit = getShopLimit(plotId) ?: return false
        val shopCount = getPlotShopCount(plotId)
        return shopCount < shopLimit // Return true if under the limit
    }

    suspend fun getPlotShopCount(plotId: UUID) = shopService.getShopCount(plotId)

    suspend fun getShopLimit(plotId: UUID): Int? {
        val plotShop = plotService.getPlotShop(plotId) ?: return null
        val plotType = plotService.getPlotType(plotId) ?: return null
        return when(plotType) {
            PlotType.GUILD -> guildLevels[plotShop.level].value
            PlotType.PERSONAL -> personalLevels[plotShop.level].value
        }
    }

    suspend fun getMaximumShopLimit(plotId: UUID): Int? {
        val plotType = plotService.getPlotType(plotId) ?: return null

        return when (plotType) {
            PlotType.GUILD -> guildLevels.last().value
            PlotType.PERSONAL -> personalLevels.last().value
        }
    }

    // Setters


}