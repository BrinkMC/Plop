package com.brinkmc.plop.plot.claim

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import java.util.*

class PlotModify(instance: Plop) {
    val test = UUID.randomUUID()

    fun Plot.addMember(member: UUID) {
        this.members = listOf(test)
    }
}