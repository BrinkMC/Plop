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

    fun setViewedShop(playerId: UUID, shopId: UUID) {
        playerToShopTracker[playerId] = ShopAccess(shopId, 1, 0)
    }

    fun getViewedShop(playerId: UUID): UUID? {
        return playerToShopTracker[playerId]?.id
    }

    suspend fun cycleMultiplier(playerId: UUID) {
        val access = playerToShopTracker[playerId] ?: return
        access.cycleMultiplier()
    }

    suspend fun getMultiplier(playerId: UUID): Int? {
        val access = playerToShopTracker[playerId] ?: return null
        return access.multiplier
    }

    suspend fun setTotal(playerId: UUID, total: Int) {
        val access = playerToShopTracker[playerId] ?: return
        access.setTotal(total)
    }

    suspend fun getTotal(playerId: UUID): Int? {
        val access = playerToShopTracker[playerId] ?: return null
        return access.total
    }

    suspend fun getLimit(playerId: UUID, shopId: UUID): Int? {
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


}