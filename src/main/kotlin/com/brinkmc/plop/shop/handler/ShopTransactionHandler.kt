package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.storage.ShopCache
import java.util.UUID

class ShopTransactionHandler(override val plugin: Plop): Addon, State {


    override suspend fun load() {
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }
}