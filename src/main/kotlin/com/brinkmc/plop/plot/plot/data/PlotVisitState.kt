package com.brinkmc.plop.plot.plot.data

import java.sql.Timestamp

data class PlotVisitState(
    var visitable: Boolean = true, // Is plot visitable
    var visits: MutableList<Timestamp> = mutableListOf()
)