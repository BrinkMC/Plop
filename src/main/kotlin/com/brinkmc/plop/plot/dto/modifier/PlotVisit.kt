package com.brinkmc.plop.plot.dto.modifier

import java.sql.Timestamp

data class PlotVisit(
    private var _visitable: Boolean = true, // Is plot visitable
    private var _level: Int,
    private var _currentVisits: Int,

    val historicalVisits: MutableList<Timestamp> = mutableListOf()
) {
    val level get() = _level
    val visitable get() = _visitable
    val currentVisits get() = _currentVisits

    fun setLevel(level: Int) {
        _level = level
    }

    fun setVisitable(visitable: Boolean) {
        _visitable = visitable
    }

    fun setCurrentVisits(currentVisits: Int) {
        _currentVisits = currentVisits
    }
}