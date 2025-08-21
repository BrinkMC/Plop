package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.Fueler
import com.brinkmc.plop.plot.plot.structure.Totem
import com.brinkmc.plop.plot.plot.structure.TotemType
import org.bukkit.Location

data class PlotFueler(
    private var level: Int, // Depending on level you can't place higher level fuelers
    private val fueler: Fueler
) {
    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }
}