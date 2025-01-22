package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.Totem

data class PlotTotem(
    var level: Int,
    val totems: MutableList<Totem>,
    @Transient internal val plotType: PlotType
)