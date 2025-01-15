package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotFactoryHandler(override val plugin: Plop): Addon, State {

    val levels = mutableListOf<Int>()

    override suspend fun load() {
        try {
            val conf = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, "plot/upgrades.yml")) // Get relevant config
            val factorySection = conf.getConfigurationSection("factory.limit") ?: return // Check filled in

            for (key in factorySection.getKeys(false)) {
                levels.add(factorySection.getInt("$key.limit")) // Add a level per child node
            }
        } catch (e: ConfigurateException) {
            logger.error("Failed to configurate factory limits :(") // Didn't work did it
        }
    }

    override suspend fun kill() {
        levels.clear()
    }
}