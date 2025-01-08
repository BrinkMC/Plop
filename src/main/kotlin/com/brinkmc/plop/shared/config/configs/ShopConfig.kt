package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.config.BaseConfig

data class ShopConfig(
    override val plugin: Plop, // Must be able to access the plugin
): BaseConfig(plugin) {

    override val config = configManager.getShopConfig() // Shop config file



}
