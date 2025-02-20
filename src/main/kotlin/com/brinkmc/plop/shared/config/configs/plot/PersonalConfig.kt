package com.brinkmc.plop.shared.config.configs.plot

import com.brinkmc.plop.shared.config.serialisers.Level
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class PersonalConfig(
    @Setting("MaxSize")
    val personalPlotMaxSize: Int = 192,
    @Setting("World")
    val personalPlotWorld: String = "plot_personal",
    @Setting("Generator")
    val personalPlotWorldGenerator: String = "overworld",

    @Setting("Factory")
    val personalFactoryLevels: List<Level> = listOf(
        Level(2, 100),
        Level(3, 300),
        Level(4, 5000)
    ),

    @Setting("Size")
    val personalPlotSizeLevels: List<Level> = listOf(
        Level(64, 500),
        Level(96, 1000),
        Level(128, 2000),
        Level(160, 3000),
        Level(192, 5000)
    ),

    @Setting("Shop")
    val personalShopLevels: List<Level> = listOf(
        Level(2, 500),
        Level(3, 1000),
        Level(4, 1500)
    ),

    @Setting("Totem")
    val personalTotemLimit: List<Level> = listOf(
        Level(1, 100),
        Level(2, 200),
        Level(3, 300)
    ),

    @Setting("Visitor")
    val personalVisitorLimit: List<Level> = listOf(
        Level(5, 200),
        Level(10, 400),
        Level(15, 600)
    )
)