package com.brinkmc.plop.shared.config.serialisers

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class Level (
    @Setting("Value")
    val value: Int?,
    @Setting("Price")
    val price: Int?
)