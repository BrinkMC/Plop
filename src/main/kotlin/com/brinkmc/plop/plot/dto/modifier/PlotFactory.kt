package com.brinkmc.plop.plot.dto.modifier

data class PlotFactory(
    private var level: Int
) {
    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }
}
