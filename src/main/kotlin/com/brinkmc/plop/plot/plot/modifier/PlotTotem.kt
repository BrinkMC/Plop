package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.plot.structure.TotemType

data class PlotTotem(
    var level: Int,
    val totems: MutableList<Totem>,
    val enableLightning: Boolean,
    @Transient internal val plotType: PlotType
) {
    fun getTypes(): List<TotemType> {
        return totems.map { it.totemType }
    }

    fun getTotem(type: TotemType): Totem? {
        return totems.firstOrNull { it.totemType == type }
    }
}