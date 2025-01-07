package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon

data class MainConfig(
    override val plugin: Plop // Must be able to access the plugin
): Addon {
    val mainConfig = configManager.getMainConfig()

    var pluginName: String
        get() = mainConfig?.node("name")?.string ?: ""
        set(value) {
            configManager.getMainConfig()?.node("name")?.set(value)
        }

}