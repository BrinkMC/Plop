package com.brinkmc.plop.plot.plot.structure

import org.bukkit.Location

enum class TOTEM_TYPE {
    FireSpread,
    Gravity,
    LavaFlow,
    WaterFlow
}

data class Totem(
    val totemId: Int,
    val totemType: TOTEM_TYPE,
    val location: Location
)