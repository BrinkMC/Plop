package com.brinkmc.plop.shared.config.serialisers

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Level (
    val value: Int?,
    val price: Int?
)