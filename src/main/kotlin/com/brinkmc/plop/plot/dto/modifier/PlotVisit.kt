package com.brinkmc.plop.plot.dto.modifier

import java.sql.Timestamp
import java.util.UUID

data class PlotVisit(


    private var _visitable: Boolean = true, // Is plot visitable
    private var _level: Int,

    val currentVisits: List<UUID>,
    val historicalVisits: MutableList<Timestamp> = mutableListOf()
) {
    val level get() = _level
    val visitable get() = _visitable

    fun setLevel(level: Int) {
        _level = level
    }

    fun setVisitable(visitable: Boolean) {
        _visitable = visitable
    }
}