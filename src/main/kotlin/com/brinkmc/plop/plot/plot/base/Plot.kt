package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.data.Claim
import com.brinkmc.plop.plot.plot.data.PlotVisit
import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.plot.plot.structure.Totem
import java.util.*

/*
I find best practice for data classes / sealed classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

sealed class Plot (
    // State
    open val plotId: UUID, // Unique ID for the plot

    open val claim: Claim,
    open val totems: MutableList<Totem>,

    open val plotVisit: PlotVisit,

    // Values with configurable limits via config
    open var plotSize: PlotSize,
    open var factoryLimit: FactoryLimit,
    open var shopLimit: ShopLimit,
    open var visitorLimit: VisitorLimit

) {

    fun upgradePlotSize(newPlotSize: PlotSize) {
        plotSize = newPlotSize
    }


    fun addMember() {
        //TODO
    }

    fun removeMember() {
        //TODO
    }
}

