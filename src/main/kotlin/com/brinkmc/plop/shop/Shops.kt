package com.brinkmc.plop.shop

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.handler.ShopCreationHandler
import com.brinkmc.plop.shop.handler.ShopHandler
import com.brinkmc.plop.shop.handler.ShopTransactionHandler
import com.brinkmc.plop.shop.shop.Shop
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.libs.jdbi.v3.core.transaction.TransactionHandler
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.math.log

class Shops(override val plugin: Plop): Addon, State {

    companion object {
        const val NO_BUY_PRICE = -1.0f
        const val NO_SELL_PRICE = -1.0f
        const val MIN_BUY_PRICE = 0.0f
        const val MIN_SELL_PRICE = 0.0f
        const val MIN_BUY_LIMIT = 0
        const val MIN_QUANTITY = 0
        val EMPTY_ITEM = Material.AIR
    }

    lateinit var handler: ShopHandler
    lateinit var transHandler: ShopTransactionHandler
    lateinit var creationHandler: ShopCreationHandler

    override suspend fun load() {
        logger.info("Loading shops...")
        handler = ShopHandler(plugin)
        transHandler = ShopTransactionHandler(plugin)
        creationHandler = ShopCreationHandler(plugin)

        listOf(
            handler,
            transHandler,
            creationHandler
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