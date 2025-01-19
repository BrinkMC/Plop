package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.BaseConfig

class PlotConfig(
    override val plugin: Plop // Must be able to access the plugin
): BaseConfig(plugin) {

    override val config = configManager.getPlotConfig() // Database config file

    // Get values for the guild plot configuration
    // guildPlotSizeMax -> guild.plot-size.max in config
    var guildPlotMaxSize: Int by delegate("guild", "plot", "max-size")
    var guildPlotWorld: String by delegate("guild", "plot", "world")
    var guildPlotWorldGenerator: String by delegate("guild", "plot", "generator")

    // Plot upgrades
    var guildFactoryLevels: List<Int> by delegate("guild", "factory", "levels")
    var guildPlotSizeLevels: List<Int> by delegate("guild","size", "levels")
    var guildShopLevels: List<Int> by delegate("guild", "shop", "levels")
    var guildTotemLimit: List<Int> by delegate("guild", "totem", "levels")

    // Get values for the personal plot configuration
    var personalPlotMaxSize: Int by delegate("personal", "plot", "max-size")
    var personalPlotWorld: String by delegate("personal", "plot", "world")
    var personalPlotWorldGenerator: String by delegate("personal", "plot", "generator")

    // Plot upgrades
    var personalFactoryLevels: List<Int> by delegate("personal", "factory", "levels")
    var personalPlotSizeLevels: List<Int> by delegate("personal","size", "levels")
    var personalShopLevels: List<Int> by delegate("personal", "shop", "levels")
    var personalTotemLimit: List<Int> by delegate("personal", "totem", "levels")
}