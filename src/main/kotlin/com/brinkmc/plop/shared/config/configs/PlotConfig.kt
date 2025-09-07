package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.shared.config.configs.plot.GuildConfig
import com.brinkmc.plop.shared.config.configs.plot.NexusConfig
import com.brinkmc.plop.shared.config.configs.plot.PersonalConfig
import com.brinkmc.plop.shared.config.serialisers.Level
import com.brinkmc.plop.plot.constant.PlotType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class PlotConfig(

    @Setting("Guild")
    val guildConfig: GuildConfig,

    @Setting("Personal")
    val personalConfig: PersonalConfig,

    @Setting("Nexus")
    val nexusConfig: NexusConfig
) {
    fun getPlotMaxSize(plotType: PlotType): Int {
        return if (plotType == PlotType.GUILD) guildConfig.guildPlotMaxSize else personalConfig.personalPlotMaxSize
    }

    fun getPlotWorld(plotType: PlotType): String {
        return if (plotType == PlotType.GUILD) guildConfig.guildPlotWorld else personalConfig.personalPlotWorld
    }

    fun getPlotWorldGenerator(plotType: PlotType): String {
        return if (plotType == PlotType.GUILD) guildConfig.guildPlotWorldGenerator else personalConfig.personalPlotWorldGenerator
    }

    fun getFactoryLevels(plotType: PlotType): List<Level> {
        return if (plotType == PlotType.GUILD) guildConfig.guildFactoryLevels else personalConfig.personalFactoryLevels
    }

    fun getPlotSizeLevels(plotType: PlotType): List<Level> {
        return if (plotType == PlotType.GUILD) guildConfig.guildPlotSizeLevels else personalConfig.personalPlotSizeLevels
    }

    fun getShopLevels(plotType: PlotType): List<Level> {
        return if (plotType == PlotType.GUILD) guildConfig.guildShopLevels else personalConfig.personalShopLevels
    }

    fun getTotemLimit(plotType: PlotType): List<Level> {
        return if (plotType == PlotType.GUILD) guildConfig.guildTotemLimit else personalConfig.personalTotemLimit
    }

    fun getVisitorLimit(plotType: PlotType): List<Level> {
        return if (plotType == PlotType.GUILD) guildConfig.guildVisitorLimit else personalConfig.personalVisitorLimit
    }
}

//    // Get values for the guild plot configuration
//    // guildPlotSizeMax turns into guild.plot-size.max in config
//    private var guildPlotMaxSize: Int by delegate("guild", "plot", "max-size", defaultValue = 384)
//    private var guildPlotWorld: String by delegate("guild", "plot", "world", defaultValue = "plot_guild")
//    private var guildPlotWorldGenerator: String by delegate("guild", "plot", "generator", defaultValue = "Overworld")
//
//    // Plot upgrades
//    private var guildFactoryLevels: List<Pair<Int, Int>> by delegate("guild", "factory", "levels", defaultValue = listOf(
//        Pair(4, 300),
//        Pair(5, 500),
//        Pair(6, 1000)
//    ))
//    private var guildPlotSizeLevels: List<Pair<Int, Int>> by delegate("guild","size", "levels", defaultValue = listOf(
//        Pair(128, 1500),
//        Pair(192, 2000),
//        Pair(256, 5000),
//        Pair(320, 7000),
//        Pair(384, 10000)
//    ))
//    private var guildShopLevels: List<Pair<Int, Int>> by delegate("guild", "shop", "levels", defaultValue = listOf(
//        Pair(4, 500),
//        Pair(6, 1000),
//        Pair(8, 1500)
//    ))
//    private var guildTotemLimit: List<Pair<Int, Int>> by delegate("guild", "totem", "levels", defaultValue = listOf(
//        Pair(1, 200),
//        Pair(2, 400),
//        Pair(3, 600)
//    ))
//    private var guildVisitorLimit: List<Pair<Int, Int>> by delegate("guild", "visitor", "limit", defaultValue = listOf(
//        Pair(10, 300),
//        Pair(15, 500),
//        Pair(20, 700)
//    ))
//
//    // Get values for the personal plot configuration
//    private var personalPlotMaxSize: Int by delegate("personal", "plot", "max-size", defaultValue = 192)
//    private var personalPlotWorld: String by delegate("personal", "plot", "world", defaultValue = "plot_personal")
//    private var personalPlotWorldGenerator: String by delegate("personal", "plot", "generator", defaultValue = "Overworld")
//
//    // Plot upgrades
//    private var personalFactoryLevels: List<Pair<Int, Int>> by delegate("personal", "factory", "levels", defaultValue = listOf(
//        Pair(2, 100),
//        Pair(3, 300),
//        Pair(4, 500)
//    ))
//    private var personalPlotSizeLevels: List<Pair<Int, Int>> by delegate("personal","size", "levels", defaultValue = listOf(
//        Pair(64, 500),
//        Pair(96, 1000),
//        Pair(128, 2000),
//        Pair(160, 3000),
//        Pair(192, 5000)
//    ))
//    private var personalShopLevels: List<Pair<Int, Int>> by delegate("personal", "shop", "levels", defaultValue = listOf(
//        Pair(2, 500),
//        Pair(4, 1000),
//        Pair(6, 1500)
//    ))
//    private var personalTotemLimit: List<Pair<Int, Int>> by delegate("personal", "totem", "levels", defaultValue = listOf(
//        Pair(1, 100),
//        Pair(2, 200),
//        Pair(3, 300)
//    ))
//    private var personalVisitorLimit: List<Pair<Int, Int>> by delegate("personal", "visitor", "limit", defaultValue = listOf(
//        Pair(5, 200),
//        Pair(10, 400),
//        Pair(15, 600)
//    ))

