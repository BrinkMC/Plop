package com.brinkmc.plop.plot.plot

import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.plot.plot.structure.Totem
import org.bukkit.Location
import java.util.*

/*
I find best practice for data classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

data class Plot (
    // State
    var guild: Boolean, // Is this a guild plot?

    // Primary
    val owner: UUID,
    var members: List<UUID>?,
    val claim: Claim,

    var totems: List<Totem>,
    var visits: PlotVisits,

    // Mutable
    var plotSize: PlotSize,
    var factoryLimit: FactoryLimit,
    var shopLimit: ShopLimit,
    var visitorLimit: VisitorLimit

) {
    fun addMember() {
        //TODO
    }

    fun removeMember() {
        //TODO
    }
}

