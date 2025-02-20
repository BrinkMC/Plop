package com.brinkmc.plop.plot.plot.modifier

import org.bukkit.Location

data class PlotClaim (
    val centre: Location, // Calculate from here
    var home: Location,
    var visit: Location
)