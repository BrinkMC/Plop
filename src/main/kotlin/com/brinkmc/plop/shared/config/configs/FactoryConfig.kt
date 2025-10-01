package com.brinkmc.plop.shared.config.configs

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class FactoryConfig (

    @Setting("ViewDistance")
    val viewDistance: Int = 16,

    @Setting("Display")
    val display: List<String> = listOf(
        "TEXT: Factory",
        "TEXT: Type: <factoryType>"
    )

)