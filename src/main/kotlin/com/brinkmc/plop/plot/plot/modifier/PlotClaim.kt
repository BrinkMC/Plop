package com.brinkmc.plop.plot.plot.modifier

import org.bukkit.Location

data class PlotClaim (
    var world: String,
    val centre: Location, // Calculate from here
    var home: Location,
    var visit: Location
)