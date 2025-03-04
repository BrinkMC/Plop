package com.brinkmc.plop.plot.plot.modifier

import com.brinkmc.plop.plot.plot.base.PlotType
import java.util.UUID

data class PlotShop(
    var level: Int,
    private val shops: MutableList<UUID>,
    @Transient internal val  plotType: PlotType
) {
    fun addShop(shop: UUID) {
        shops.add(shop)
    }

    fun removeShop(shop: UUID) {
        shops.remove(shop)
    }

    fun getShops(): List<UUID> {
        return shops
    }
}