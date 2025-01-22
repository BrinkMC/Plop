package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.PlotConfig
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotFactoryHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Int>()
    private val personalLevels = mutableListOf<Int>()

    override suspend fun load() {
        guildLevels.addAll(plotConfig.getFactoryLevels(PlotType.GUILD)) // Add all guild plot size levels
        personalLevels.addAll(plotConfig.getFactoryLevels(PlotType.PERSONAL))
    }

    override suspend fun kill() {
        guildLevels.clear() // Remove all values
        personalLevels.clear()
    }

    fun getCurrentFactoryLimit(plotType: PlotType, level: Int): Int { // Get current limit based on level
        return when (plotType) {
            PlotType.GUILD -> guildLevels[level]
            PlotType.PERSONAL -> personalLevels[level]
        }
    }

    fun getMaximumFactoryLimit(plotType: PlotType): Int {
        return when (plotType) {
            PlotType.GUILD -> plotConfig.getFactoryLevels(PlotType.GUILD).last() // Last gets largest item
            PlotType.PERSONAL -> plotConfig.getFactoryLevels(PlotType.PERSONAL).last()
        }
    }


}