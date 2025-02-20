package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.serialisers.Level
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotShopHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Level>()
    private val personalLevels = mutableListOf<Level>()

    override suspend fun load() {
        plotConfig.getShopLevels(PlotType.GUILD)?.let { guildLevels.addAll(it) } // Add all guild plot size levels
        plotConfig.getShopLevels(PlotType.PERSONAL)?.let { personalLevels.addAll(it) }
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

    fun getCurrentShopLimit(plotType: PlotType, level: Int): Int {
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level].value ?: -1
            PlotType.PERSONAL -> personalLevels[level].value ?: -1
        }
    }

    fun getMaximumPlotSize(plotType: PlotType) : Int {
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