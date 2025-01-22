package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import kotlinx.atomicfu.AtomicInt
import org.bukkit.Location
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.mutableListOf

data class PlotFactory(
    var level: Int,
    val factories: MutableList<Location>,
    @Transient internal val plotType: PlotType
)
