package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.BukkitUtils
import com.brinkmc.plop.shop.constant.TransactionResult
import com.brinkmc.plop.shop.dto.Shop
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.text.compareTo
import kotlin.times

class ShopTransactionService(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()
    // Prevent duplication of items by ensuring that only one coroutine can access the shop at a time

    override suspend fun load() {

    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    private suspend fun shopHasBalance(shopId: UUID, playerId: UUID): Boolean {
        val shopOwnerId = shopService.getShopOwnerId(shopId) ?: return false
        val shopBalance = economyService.getBalance(shopOwnerId) ?: return false
        val shopPrice = shopService.getShopPrice(shopId) ?: return false
        val selectedTotal: Int = shopAccessService.getTotal(playerId) ?: return false
        return shopPrice * selectedTotal <= shopBalance
    }

    private suspend fun shopSellLimitReached(shopId: UUID, playerId: UUID): Boolean {
        val selectedTotal: Int = shopAccessService.getTotal(playerId) ?: return true
        val shopSellLimit: Int = shopService.getShopSellLimit(shopId) ?: return true
        return selectedTotal > shopSellLimit
    }

    private suspend fun playerHasBalance(shopId: UUID, playerId: UUID): Boolean {
        val playerBalance = economyService.getBalance(playerId) ?: return false
        val shopPrice = shopService.getShopPrice(shopId) ?: return false
        val selectedTotal: Int = shopAccessService.getTotal(playerId) ?: return false
        return shopPrice * selectedTotal <= playerBalance
    }

    private suspend fun shopHasStock(shopId: UUID, playerId: UUID): Boolean {
        val selectedTotal: Int = shopAccessService.getTotal(playerId) ?: return false
        val shopQuantity: Int = shopService.getShopQuantity(shopId) ?: return false
        return shopQuantity >= selectedTotal
    }

    private suspend fun playerHasStock(shopId: UUID, playerId: UUID): Boolean {
        val shopItem = shopService.getShopItem(shopId) ?: return false
        val selectedTotal = shopAccessService.getTotal(playerId) ?: return false
        val totalPlayerStock = BukkitUtils.countItemsInInventory(playerId, shopItem) ?: return false
        return totalPlayerStock >= (selectedTotal * shopItem.amount)
    }

    suspend fun initialiseBuyTransaction(playerId: UUID, shopId: UUID): TransactionResult = mutex.withLock {
        if (!shopHasStock(shopId, playerId)) {
            return TransactionResult.SHOP_INSUFFICIENT_STOCK
        }
        if (!playerHasBalance(shopId, playerId)) {
            return TransactionResult.PLAYER_INSUFFICIENT_BALANCE
        }

        val shopPrice = shopService.getShopPrice(shopId) ?: return TransactionResult.FAILURE
        val shopQuantity = shopService.getShopQuantity(shopId) ?: return TransactionResult.FAILURE
        val shopOwnerId = shopService.getShopOwnerId(shopId) ?: return TransactionResult.FAILURE
        val shopItem = shopService.getShopItem(shopId) ?: return TransactionResult.FAILURE
        val selectedTotal = shopAccessService.getTotal(playerId) ?: return TransactionResult.FAILURE
        val totalPrice = shopPrice * selectedTotal

        if (!economyService.withdrawBalance(playerId, totalPrice)) return TransactionResult.FAILURE
        if (!economyService.depositBalance(shopOwnerId, totalPrice)) {
            economyService.depositBalance(playerId, totalPrice) // rollback
            return TransactionResult.FAILURE
        }

        for (i in 0 until selectedTotal) {
            val result = playerService.addToInventory(playerId, shopItem)
            if (!result) {
                economyService.depositBalance(playerId, totalPrice)
                economyService.withdrawBalance(shopOwnerId, totalPrice)
                for (j in 0 until i) {
                    playerService.removeItemFromInventory(playerId, shopItem)
                }
                return TransactionResult.FAILURE
            }
        }

        shopService.setShopQuantity(shopId, shopQuantity - selectedTotal)
        shopService.addTransaction(shopId, playerId, selectedTotal, totalPrice)
        return TransactionResult.SUCCESS
    }

    suspend fun initialiseSellTransaction(playerId: UUID, shopId: UUID): TransactionResult = mutex.withLock {
        if (shopSellLimitReached(shopId, playerId)) {
            return TransactionResult.BUY_LIMIT_REACHED
        }
        if (!playerHasStock(shopId, playerId)) {
            return TransactionResult.PLAYER_INSUFFICIENT_STOCK
        }
        if (!shopHasBalance(shopId, playerId)) {
            return TransactionResult.SHOP_INSUFFICIENT_BALANCE
        }

        val shopPrice = shopService.getShopPrice(shopId) ?: return TransactionResult.FAILURE
        val shopOwnerId = shopService.getShopOwnerId(shopId) ?: return TransactionResult.FAILURE
        val shopBalance = economyService.getBalance(shopOwnerId) ?: return TransactionResult.FAILURE
        val shopItem = shopService.getShopItem(shopId) ?: return TransactionResult.FAILURE
        val selectedTotal = shopAccessService.getTotal(playerId) ?: return TransactionResult.FAILURE
        val totalPrice = shopPrice * selectedTotal

        for (i in 0 until selectedTotal) {
            val removed = playerService.removeItemFromInventory(playerId, shopItem)
            if (!removed) {
                for (j in 0 until i) {
                    playerService.addToInventory(playerId, shopItem)
                }
                return TransactionResult.FAILURE
            }
        }

        if (!economyService.withdrawBalance(shopOwnerId, totalPrice)) {
            for (i in 0 until selectedTotal) {
                playerService.addToInventory(playerId, shopItem)
            }
            return TransactionResult.FAILURE
        }

        if (!economyService.depositBalance(playerId, totalPrice)) {
            economyService.depositBalance(shopOwnerId, totalPrice)
            for (i in 0 until selectedTotal) {
                playerService.addToInventory(playerId, shopItem)
            }
            return TransactionResult.FAILURE
        }

        val shopQuantity = shopService.getShopQuantity(shopId) ?: 0
        shopService.setShopQuantity(shopId, shopQuantity + selectedTotal)
        shopService.addTransaction(shopId, playerId, selectedTotal, totalPrice)
        return TransactionResult.SUCCESS
    }

//    private fun checkTransaction(player: Player, shop: Shop, amount: Int, type: ShopType): TransactionResult {
//        return when(type) {
//            ShopType.BUY -> {
//                if (amount == 0) {
//                    return TransactionResult.FAILURE
//                }
//                if (!playerHasStock(player, shop, amount)) {
//                    return TransactionResult.PLAYER_INSUFFICIENT_STOCK
//                }
//                if (!shopHasBalance(shop, amount)) {
//                    return TransactionResult.SHOP_INSUFFICIENT_BALANCE
//                }
//                if (shopBuyLimitReached(shop, amount)) {
//                    return TransactionResult.BUY_LIMIT_REACHED
//                }
//                return TransactionResult.SUCCESS
//
//            }
//            ShopType.SELL -> {
//                if (amount == 0) {
//                    return TransactionResult.FAILURE
//                }
//                if (!shopHasStock(shop, amount)) {
//                    return TransactionResult.SHOP_INSUFFICIENT_STOCK
//                }
//                if (!playerHasBalance(player, shop, amount)) {
//                    return TransactionResult.PLAYER_INSUFFICIENT_BALANCE
//                }
//                return TransactionResult.SUCCESS
//            }
//        }
//    }
//
//    suspend fun playerPurchase(player: Player, shop: Shop, amount: Int): TransactionResult = mutex.withLock {
//
//        if (!shopHasStock(shop, amount)) {
//            return TransactionResult.SHOP_INSUFFICIENT_STOCK
//        }
//
//        if (!playerHasBalance(player, shop, amount)) {
//            return TransactionResult.PLAYER_INSUFFICIENT_BALANCE
//        }
//
//        // Proceed with transaction
//
//        economy.withdraw(player.uniqueId, (shop.sellPrice * amount).toDouble()) // WITHDRAW MONEY FROM PLAYER
//        shop.owner.depositBalance(economy, (shop.sellPrice * amount).toDouble()) // DEPOSIT MONEY TO SHOP OWNER
//        for (i in 0 until amount) {
//            player.inventory.addItem(shop.item) // ADD ITEM TO PLAYER INVENTORY
//        }
//
//        shop.setQuantity(shop.quantity - (amount * shop.item.amount)) // SET SHOP QUANTITY
//        shop.addTransaction(player.uniqueId, amount, ShopType.SELL)
//        return TransactionResult.SUCCESS
//    }
//
//    suspend fun playerSell(player: Player, shop: Shop, amount: Int): TransactionResult = mutex.withLock {
//
//        if (!playerHasStock(player, shop, amount)) {
//            return TransactionResult.PLAYER_INSUFFICIENT_STOCK
//        }
//
//        if (!shopHasBalance(shop, amount)) {
//            return TransactionResult.SHOP_INSUFFICIENT_BALANCE
//        }
//
//        if (amount > shop.buyLimit) {
//            return TransactionResult.BUY_LIMIT_REACHED
//        }
//
//        economy.deposit(player.uniqueId, (shop.buyPrice * amount).toDouble())
//        shop.owner.withdrawBalance(economy, (shop.buyPrice * amount).toDouble())
//        for (i in 0 until amount) {
//            player.inventory.removeItemAnySlot(shop.item)
//        }
//        shop.setBuyLimit(shop.buyLimit - amount)// Update buy limit
//        shop.setQuantity(shop.quantity + (amount * shop.item.amount))
//
//        shop.addTransaction(player.uniqueId, amount, ShopType.BUY)
//        return TransactionResult.SUCCESS
//    }

}
