package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.config.BaseConfig
import org.spongepowered.configurate.ConfigurationNode

data class TotemConfig(
    override val plugin: Plop // Must be able to access the plugin
): BaseConfig(plugin) {

    override suspend fun loadConfig(): ConfigurationNode? {
        return configManager.getTotemConfig()
    } // Totem config file

}