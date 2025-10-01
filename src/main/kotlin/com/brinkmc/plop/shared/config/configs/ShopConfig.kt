package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ShopConfig(

    @Setting("ViewDistance")
    val viewDistance: Int = 100,

    @Setting("Display")
    val display: List<String> = listOf(
        "TEXT:<red>Shop - <owner>",
        "TEXT: Price <price> | Available <available>",
        "TEXT: Sell Price <price>",
        "TEXT:<shopItem>"
    ),
)