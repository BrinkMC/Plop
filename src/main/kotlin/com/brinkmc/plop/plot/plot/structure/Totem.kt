package com.brinkmc.plop.plot.plot.structure

import com.brinkmc.plop.shared.util.plot.TotemType
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Location
import org.bukkit.World

data class Totem(
    private val totemType: TotemType,
    private val location: Location
) {
    fun getTotemType(): TotemType {
        return totemType
    }

    fun getLocation(): Location {
        return location
    }

    fun strike() {
        location.world.strikeLightningEffect(location)
    }
}