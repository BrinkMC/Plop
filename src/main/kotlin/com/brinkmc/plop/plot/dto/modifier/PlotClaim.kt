package com.brinkmc.plop.plot.dto.modifier

import org.bukkit.Location

data class PlotClaim (
    val centre: Location, // Calculate from here
    private var _home: Location,
    private var _visit: Location
) {
    val home: Location
        get() = _home
    val visit: Location
        get() = _visit

    fun setHome(location: Location) {
        _home = location
    }

    fun setVisit(location: Location) {
        _visit = location
    }
}