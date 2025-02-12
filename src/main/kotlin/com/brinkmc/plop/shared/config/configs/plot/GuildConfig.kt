package com.brinkmc.plop.shared.config.configs.plot

import com.brinkmc.plop.shared.config.serialisers.Level
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
data class GuildConfig(
    @Setting("MaxSize")
    val guildPlotMaxSize: Int?,
    @Setting("World")
    val guildPlotWorld: String?,
    @Setting("Generator")
    val guildPlotWorldGenerator: String?,

    @Setting("Factory")
    val guildFactoryLevels: List<Level>?,

    @Setting("Size")
    val guildPlotSizeLevels: List<Level>?,

    @Setting("Shop")
    val guildShopLevels: List<Level>?,

    @Setting("Totem")
    val guildTotemLimit: List<Level>?,

    @Setting("Visitor")
    val guildVisitorLimit: List<Level>?
)