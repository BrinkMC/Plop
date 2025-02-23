package com.brinkmc.plop.plot.plot.structure

import org.bukkit.Location

enum class TotemType {
    FIRE_SPREAD,
    LAVA_FLOW,
    WATER_FLOW,
    ICE_MELT,
    LEAF_DECAY,
}

data class Totem(
    val totemId: Int,
    val totemType: TotemType,
    val location: Location
)