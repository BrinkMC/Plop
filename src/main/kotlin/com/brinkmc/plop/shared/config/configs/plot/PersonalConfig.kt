package com.brinkmc.plop.shared.config.configs.plot

import com.brinkmc.plop.shared.config.serialisers.Level
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class PersonalConfig(
    @Setting("MaxSize")
    val personalPlotMaxSize: Int?,
    @Setting("World")
    val personalPlotWorld: String?,
    @Setting("Generator")
    val personalPlotWorldGenerator: String?,

    @Setting("Factory")
    val personalFactoryLevels: List<Level>?,

    @Setting("Size")
    val personalPlotSizeLevels: List<Level>?,

    @Setting("Shop")
    val personalShopLevels: List<Level>?,

    @Setting("Totem")
    val personalTotemLimit: List<Level>?,

    @Setting("Visitor")
    val personalVisitorLimit: List<Level>?
)