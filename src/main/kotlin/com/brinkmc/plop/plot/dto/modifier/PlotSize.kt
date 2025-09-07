package com.brinkmc.plop.plot.dto.modifier


data class PlotSize(
    private var level: Int
) {
    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }
}