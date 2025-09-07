package com.brinkmc.plop.plot.dto.modifier

import com.brinkmc.plop.plot.dto.structure.Fueler

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