package com.brinkmc.plop.plot.plot.structure

import kotlinx.coroutines.CoroutineScope
import org.bukkit.Location
import org.bukkit.World

enum class TotemType {
    FIRE_SPREAD,
    LAVA_FLOW,
    WATER_FLOW,
    ICE_MELT,
    LEAF_DECAY,
}

data class Totem(
    val totemType: TotemType,
    val location: Location
) {
    fun strike() {
        location.world.strikeLightningEffect(location)
    }
}