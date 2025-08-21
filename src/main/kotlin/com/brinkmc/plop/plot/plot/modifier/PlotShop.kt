package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import java.util.UUID

data class PlotShop(
    private var level: Int
) {
    fun getLevel(): Int {
        return level
    }

    fun upgradeLevel() {
        level++
    }
}