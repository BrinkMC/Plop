package com.brinkmc.plop.shop

import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.plot.plot.structure.Totem
import org.bukkit.Location
import org.bukkit.entity.Item
import java.util.UUID

data class Shop (
    // Primary
    val id: UUID,

    val locationX: Location,
    val creator: UUID, // Original placer
    var plot: UUID, // Is it on a plot?

    // Mutable
    var ware: Item,
    var stock: Int,

    var price: Float

) {
    fun addMember() {
        //TODO
    }

    fun removeMember() {
        //TODO
    }
}

