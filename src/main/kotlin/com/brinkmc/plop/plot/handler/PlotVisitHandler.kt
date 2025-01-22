package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State

class PlotVisitHandler(override val plugin: Plop): Addon, State {

    private val guildLevels = mutableListOf<Int>()
    private val personalLevels = mutableListOf<Int>()

    override suspend fun load() {
        guildLevels.addAll(plotConfig.getVisitorLimit(PlotType.GUILD)) // Add all guild plot size levels
        personalLevels.addAll(plotConfig.getVisitorLimit(PlotType.PERSONAL))
    }

    override suspend fun kill() {
        guildLevels.clear()
        personalLevels.clear()
    }

    fun addVisitor() {

    }
}