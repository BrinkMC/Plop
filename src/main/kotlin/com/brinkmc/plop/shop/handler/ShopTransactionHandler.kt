package com.brinkmc.plop.shop.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.shop.TransactionResult
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.brinkmc.plop.shop.storage.ShopCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.entity.Player
import java.util.UUID

class ShopTransactionHandler(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()
    // Prevent duplication of items by ensuring that only one coroutine can access the shop at a time

    override suspend fun load() {

    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    // Amount as in amount of bundles being purchased
    fun shopHasStock(shop: Shop, amount: Int): Boolean {
        return shop.quantity >= (amount * shop.item.amount)
    }

    fun shopHasBalance(shop: Shop, amount: Int): Boolean {
        return shop.buyPrice * amount <= shop.owner.getBalance(economy)
    }

    fun shopBuyLimitReached(shop: Shop, amount: Int): Boolean {
        return amount > shop.buyLimit
    }

    fun playerHasStock(player: Player, shop: Shop, amount: Int): Boolean {
        return player.inventory.getAmountOf(shop.item) >= amount * shop.item.amount
    }

    fun playerHasBalance(player: Player, shop: Shop, amount: Int): Boolean {
        return shop.sellPrice * amount <= economy.getBalance(player.uniqueId)
    }

    fun checkTransaction(player: Player, shop: Shop, amount: Int, type: ShopType): TransactionResult {
        return when(type) {
            ShopType.BUY -> {
                if (amount == 0) {
                    return TransactionResult.FAILURE
                }
                if (!playerHasStock(player, shop, amount)) {
                    return TransactionResult.PLAYER_INSUFFICIENT_STOCK
                }
                if (!shopHasBalance(shop, amount)) {
                    return TransactionResult.SHOP_INSUFFICIENT_BALANCE
                }
                if (shopBuyLimitReached(shop, amount)) {
                    return TransactionResult.BUY_LIMIT_REACHED
                }
                return TransactionResult.SUCCESS

            }
            ShopType.SELL -> {
                if (amount == 0) {
                    return TransactionResult.FAILURE
                }
                if (!shopHasStock(shop, amount)) {
                    return TransactionResult.SHOP_INSUFFICIENT_STOCK
                }
                if (!playerHasBalance(player, shop, amount)) {
                    return TransactionResult.PLAYER_INSUFFICIENT_BALANCE
                }
                return TransactionResult.SUCCESS
            }
        }
    }

    suspend fun playerPurchase(player: Player, shop: Shop, amount: Int): TransactionResult = mutex.withLock {

        if (!shopHasStock(shop, amount)) {
            return TransactionResult.SHOP_INSUFFICIENT_STOCK
        }

        if (!playerHasBalance(player, shop, amount)) {
            return TransactionResult.PLAYER_INSUFFICIENT_BALANCE
        }

        // Proceed with transaction

        economy.withdraw(player.uniqueId, (shop.sellPrice * amount).toDouble()) // WITHDRAW MONEY FROM PLAYER
        shop.owner.depositBalance(economy, (shop.sellPrice * amount).toDouble()) // DEPOSIT MONEY TO SHOP OWNER
        for (i in 0 until amount) {
            player.inventory.addItem(shop.item) // ADD ITEM TO PLAYER INVENTORY
        }

        shop.setQuantity(shop.quantity - (amount * shop.item.amount)) // SET SHOP QUANTITY
        shop.addTransaction(player.uniqueId, amount, ShopType.SELL)
        return TransactionResult.SUCCESS
    }

    suspend fun playerSell(player: Player, shop: Shop, amount: Int): TransactionResult = mutex.withLock {

        if (!playerHasStock(player, shop, amount)) {
            return TransactionResult.PLAYER_INSUFFICIENT_STOCK
        }

        if (!shopHasBalance(shop, amount)) {
            return TransactionResult.SHOP_INSUFFICIENT_BALANCE
        }

        if (amount > shop.buyLimit) {
            return TransactionResult.BUY_LIMIT_REACHED
        }

        economy.deposit(player.uniqueId, (shop.buyPrice * amount).toDouble())
        shop.owner.withdrawBalance(economy, (shop.buyPrice * amount).toDouble())
        for (i in 0 until amount) {
            player.inventory.removeItemAnySlot(shop.item)
        }
        shop.setBuyLimit(shop.buyLimit - amount)// Update buy limit
        shop.setQuantity(shop.quantity + (amount * shop.item.amount))

        shop.addTransaction(player.uniqueId, amount, ShopType.BUY)
        return TransactionResult.SUCCESS
    }

}
