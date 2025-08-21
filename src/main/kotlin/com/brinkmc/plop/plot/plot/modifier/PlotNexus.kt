package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.Fueler
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.plot.structure.TotemType
import org.bukkit.Location

data class PlotNexus(
    private val nexus: MutableList<Nexus>
) {
    fun getNexuses(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }
}