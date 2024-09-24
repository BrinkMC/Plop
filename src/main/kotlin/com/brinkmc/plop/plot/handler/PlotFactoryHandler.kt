package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.configuration.file.YamlConfiguration
import org.spongepowered.configurate.ConfigurateException
import java.io.File

class PlotFactoryHandler(override val plugin: Plop): Addon, State {

    val lim = mutableMapOf<Int, Int>()

    override fun load() {
        try {
            val conf = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, "plot/upgrades.yml"))
            val factorySection = conf.getConfigurationSection("factory.limit") ?: return

            for (key in factorySection.getKeys(false)) {
                lim[factorySection.getInt("$key.level")] = factorySection.getInt("$key.limit")
            }
        } catch (e: ConfigurateException) {
            logger.error("Failed to configurate factory limits :(")
        }
    }

    override fun kill() {
        TODO("Not yet implemented")
    }
}