package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.PlotConfig
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotFactoryHandler(override val plugin: Plop): Addon, State {

    val levels = mutableListOf<Int>()

    override suspend fun load() {
        plotConfig.factoryLevels
    }

    override suspend fun kill() {
        levels.clear()
    }
}