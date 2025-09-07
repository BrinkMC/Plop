package com.brinkmc.plop.plot.dto.structure

import com.brinkmc.plop.plot.constant.TotemType
import org.bukkit.Location

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