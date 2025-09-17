package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.BukkitUtils
import com.brinkmc.plop.shop.constant.ShopType
import com.brinkmc.plop.shop.dto.ShopAccess
import java.util.UUID

class ShopAccessService(override val plugin: Plop): Addon, State {

    private val playerToShopTracker = hashMapOf<UUID, ShopAccess>()

    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    private suspend fun getMultiplier(playerId: UUID): Int? {
        val access = playerToShopTracker[playerId] ?: return null
        return access.multiplier
    }

    private suspend fun setTotal(playerId: UUID, total: Int) {
        val access = playerToShopTracker[playerId] ?: return
        access.setTotal(total)
    }


    private suspend fun getLimit(playerId: UUID, shopId: UUID): Int? {
        return when (shopService.getShopType(shopId)) {
            ShopType.BUY -> {
                shopService.getShopQuantity(shopId)
            }

            ShopType.SELL -> {
                BukkitUtils.countItemsInInventory(playerId, shopService.getShopItem(shopId)) ?: return null
            }

            else -> {
                0
            }
        }
    }

    // Public
    fun updateAccessShop(playerId: UUID, shopId: UUID) {
        val shopAccess = ShopAccess(shopId, 1, 0)
        playerToShopTracker[playerId] = shopAccess
    }

    suspend fun getTotal(playerId: UUID): Int? {
        val access = playerToShopTracker[playerId] ?: return null
        return access.total
    }

    fun getViewedShop(playerId: UUID): UUID? {
        return playerToShopTracker[playerId]?.id
    }

    suspend fun increment(playerId: UUID): Boolean { // succeeded or failed
        val shopId = shopAccessService.getViewedShop(playerId) ?: return false

        val total = getTotal(playerId) ?: return false
        val multiplier = getMultiplier(playerId) ?: return false
        val limit = getLimit(playerId, shopId) ?: return false

        if (total + multiplier > limit) {
            return false
        }

        setTotal(playerId, total + multiplier)
        return true
    }

    suspend fun decrement(playerId: UUID): Boolean { // succeeded or failed
        val total = getTotal(playerId) ?: return false
        val multiplier = getMultiplier(playerId) ?: return false

        if (total - multiplier < 0) {
            return false
        }

        setTotal(playerId, total - multiplier)
        return true
    }

    suspend fun cycleMultiplier(playerId: UUID) {
        val access = playerToShopTracker[playerId] ?: return
        access.cycleMultiplier()
    }
}
