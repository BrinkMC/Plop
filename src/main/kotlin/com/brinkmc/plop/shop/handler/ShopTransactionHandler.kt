package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.storage.ShopCache
import java.util.UUID

class ShopTransactionHandler(override val plugin: Plop): Addon, State {

    private lateinit var shopCache: ShopCache

    override suspend fun load() {
        shopCache = ShopCache(plugin)

        shopCache.load()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    suspend fun getShops(plotId: UUID): List<Shop> { // Get all shops which have plotId of some value
        return plots.handler.getPlotById(plotId)?.shop?.shops?.mapNotNull { getShop(it) }?.toList() ?: listOf()
    }

    suspend fun getShop(shopId: UUID): Shop? {
        return shopCache.getShop(shopId)
    }
}