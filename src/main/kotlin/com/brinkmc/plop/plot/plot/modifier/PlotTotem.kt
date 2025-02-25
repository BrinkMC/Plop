package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.plot.structure.TotemType
import org.bukkit.Location

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

    fun addTotem(type: TotemType, location: Location) {
        totems.add(Totem(type, location))
    }

    fun removeTotem(type: TotemType, location: Location) {
        totems.removeIf { it.totemType == type && it.location == location }
    }
}