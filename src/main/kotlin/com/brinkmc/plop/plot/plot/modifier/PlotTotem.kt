package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.shared.util.plot.TotemType
import org.bukkit.Location

data class PlotTotem(
    private var level: Int,
    private val totems: MutableList<Totem>,
    private var enableLightning: Boolean
) {
    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }

    fun getTypes(): List<TotemType> {
        return totems.map { it.getTotemType() }
    }

    fun getTotems(): List<Totem> {
        return totems
    }

    fun addTotem(type: TotemType, location: Location) {
        totems.add(Totem(type, location))
    }

    fun removeTotem(type: TotemType, location: Location) {
        totems.removeIf { it.getTotemType() == type && it.getLocation() == location }
    }
}