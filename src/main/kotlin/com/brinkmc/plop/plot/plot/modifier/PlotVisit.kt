package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import java.sql.Timestamp

data class PlotVisit(
    var visitable: Boolean = true, // Is plot visitable
    var level: Int,
    var currentVisits: Int,
    val historicalVisits: MutableList<Timestamp> = mutableListOf(),
    @Transient internal val plotType: PlotType
)