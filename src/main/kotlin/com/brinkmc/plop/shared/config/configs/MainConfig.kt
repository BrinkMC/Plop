package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.config.BaseConfig
import org.spongepowered.configurate.ConfigurationNode

data class MainConfig(
    override val plugin: Plop // Must be able to access the plugin
): BaseConfig(plugin) {

    override suspend fun loadConfig(): ConfigurationNode? {
        return configManager.getMainConfig()
    }

    var pluginName: String by delegate("plugin-name")

}