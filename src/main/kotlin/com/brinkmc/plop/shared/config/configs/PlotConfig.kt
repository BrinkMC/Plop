package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.BaseConfig
import kotlinx.coroutines.sync.withLock

class PlotConfig(
    override val plugin: Plop // Must be able to access the plugin
): BaseConfig(plugin) {

    override val config = configManager.getPlotConfig() // Database config file

    // Get values for the guild plot configuration
    // guildPlotSizeMax -> guild.plot-size.max in config
    private var guildPlotMaxSize: Int by delegate("guild", "plot", "max-size")
    private var guildPlotWorld: String by delegate("guild", "plot", "world")
    private var guildPlotWorldGenerator: String by delegate("guild", "plot", "generator")

    // Plot upgrades
    private var guildFactoryLevels: List<Int> by delegate("guild", "factory", "levels")
    private var guildPlotSizeLevels: List<Int> by delegate("guild","size", "levels")
    private var guildShopLevels: List<Int> by delegate("guild", "shop", "levels")
    private var guildTotemLimit: List<Int> by delegate("guild", "totem", "levels")
    private var guildVisitorLimit: List<Int> by delegate("guild", "visitor", "limit")

    // Get values for the personal plot configuration
    private var personalPlotMaxSize: Int by delegate("personal", "plot", "max-size")
    private var personalPlotWorld: String by delegate("personal", "plot", "world")
    private var personalPlotWorldGenerator: String by delegate("personal", "plot", "generator")

    // Plot upgrades
    private var personalFactoryLevels: List<Int> by delegate("personal", "factory", "levels")
    private var personalPlotSizeLevels: List<Int> by delegate("personal","size", "levels")
    private var personalShopLevels: List<Int> by delegate("personal", "shop", "levels")
    private var personalTotemLimit: List<Int> by delegate("personal", "totem", "levels")
    private var personalVisitorLimit: List<Int> by delegate("personal", "visitor", "limit")

    fun getPlotMaxSize(plotType: PlotType): Int {
        return if (plotType == PlotType.GUILD) guildPlotMaxSize else personalPlotMaxSize
    }

    fun getPlotWorld(plotType: PlotType): String {
        return if (plotType == PlotType.GUILD) guildPlotWorld else personalPlotWorld
    }

    fun getPlotWorldGenerator(plotType: PlotType): String {
        return if (plotType == PlotType.GUILD) guildPlotWorldGenerator else personalPlotWorldGenerator
    }

    fun getFactoryLevels(plotType: PlotType): List<Int> {
        return if (plotType == PlotType.GUILD) guildFactoryLevels else personalFactoryLevels
    }

    fun getPlotSizeLevels(plotType: PlotType): List<Int> {
        return if (plotType == PlotType.GUILD) guildPlotSizeLevels else personalPlotSizeLevels
    }

    fun getShopLevels(plotType: PlotType): List<Int> {
        return if (plotType == PlotType.GUILD) guildShopLevels else personalShopLevels
    }

    fun getTotemLimit(plotType: PlotType): List<Int> {
        return if (plotType == PlotType.GUILD) guildTotemLimit else personalTotemLimit
    }

    fun getVisitorLimit(plotType: PlotType): List<Int> {
        return if (plotType == PlotType.GUILD) guildVisitorLimit else personalVisitorLimit
    }
}