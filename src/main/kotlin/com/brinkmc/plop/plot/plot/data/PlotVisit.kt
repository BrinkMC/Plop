package com.brinkmc.plop.plot.plot.data

import java.util.Date

data class PlotVisit(
    var open: Boolean = true, // Is plot visitable
    var visits: List<Date> = listOf()
)