package com.brinkmc.plop.plot.dto.modifier

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