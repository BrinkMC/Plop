package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.config.BaseConfig

data class MainConfig(
    override val plugin: Plop // Must be able to access the plugin
): BaseConfig(plugin) {

    override val config = configManager.getMainConfig()

    var pluginName: String by delegate("plugin-name")

}