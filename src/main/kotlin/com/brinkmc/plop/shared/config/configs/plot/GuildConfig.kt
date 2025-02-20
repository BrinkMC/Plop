package com.brinkmc.plop.shared.config.configs.plot

import com.brinkmc.plop.shared.config.serialisers.Level
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
data class GuildConfig(
    @Setting("MaxSize")
    val guildPlotMaxSize: Int = 384,
    @Setting("World")
    val guildPlotWorld: String = "plot_guild",
    @Setting("Generator")
    val guildPlotWorldGenerator: String = "overworld",

    @Setting("Factory")
    val guildFactoryLevels: List<Level> = listOf(
        Level(4, 300),
        Level(5, 500),
        Level(6, 1000)
    ),

    @Setting("Size")
    val guildPlotSizeLevels: List<Level> = listOf(
        Level(128, 1500),
        Level(192, 2000),
        Level(256, 5000),
        Level(320, 7500),
        Level(384, 10000)
    ),

    @Setting("Shop")
    val guildShopLevels: List<Level> = listOf(
        Level(4, 500),
        Level(6, 1000),
        Level(8, 1500)
    ),

    @Setting("Totem")
    val guildTotemLimit: List<Level> = listOf(
        Level(1, 200),
        Level(2, 400),
        Level(3, 600)
    ),

    @Setting("Visitor")
    val guildVisitorLimit: List<Level> = listOf(
        Level(10, 300),
        Level(15, 500),
        Level(20, 700)
    )
)