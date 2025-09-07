package com.brinkmc.plop.plot.dto.modifier

import org.bukkit.Location

data class PlotClaim (
    private val centre: Location, // Calculate from here
    private var home: Location,
    private var visit: Location
) {
    fun getCentre(): Location {
        return centre
    }

    fun setHome(location: Location) {
        home = location
    }
    fun getHome(): Location {
        return home
    }

    fun setVisit(location: Location) {
        visit = location
    }
    fun getVisit(): Location {
        return visit
    }
}