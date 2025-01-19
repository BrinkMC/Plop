package com.brinkmc.plop.plot.plot.data

import org.bukkit.Location


data class Claim (
    var world: String,
    val centre: Location, // Calculate from here
    var home: Location,
    var visit: Location
) {
    fun getCorner() {

    }
}