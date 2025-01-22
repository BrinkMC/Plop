package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType

data class PlotVisitLimit(
    var level: Int,
    var currentAmount: Int,
    @Transient internal val plotType: PlotType
)