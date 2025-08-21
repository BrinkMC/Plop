package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import org.bukkit.Location
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.mutableListOf

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
