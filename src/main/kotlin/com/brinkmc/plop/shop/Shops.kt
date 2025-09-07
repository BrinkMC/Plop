package com.brinkmc.plop.shop

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.service.ShopCreationService
import com.brinkmc.plop.shop.service.ShopService
import com.brinkmc.plop.shop.service.ShopStockService
import com.brinkmc.plop.shop.service.ShopTransactionService

class Shops(override val plugin: Plop): Addon, State {

    override val shopService = ShopService(plugin)
    override val shopTransactionService = ShopTransactionService(plugin)
    override val shopCreationService = ShopCreationService(plugin)
    override val shopStockService = ShopStockService(plugin)

    override suspend fun load() {
        logger.info("Loading shops...")
        listOf(
            shopService,
            shopTransactionService,
            shopCreationService,
            shopStockService
        ).forEach { handler -> (handler as State).load() }
    }

    override suspend fun kill() {
        listOf(
            handler,
            transHandler,
            creationHandler
        ).forEach { handler -> (handler as State).kill() }
    }
}