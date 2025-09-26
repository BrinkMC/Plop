package com.brinkmc.plop.shop.dto

import com.brinkmc.plop.shop.constant.ShopType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class ShopCreation(
    val id: UUID,
    val location: String,

    private var _shopType: ShopType?,
    private var _item: ItemStack?,
    private var _quantity: Int?,
    private var _price: Double?,
    private var _open: Boolean?,

    private var _sellLimit: Int?,
) {
    private val mutex = Mutex()

    val shopType: ShopType? get() = _shopType
    val item: ItemStack? get() = _item
    val quantity: Int? get() = _quantity
    val price: Double? get() = _price
    val open: Boolean? get() = _open
    val sellLimit: Int? get() = _sellLimit

    suspend fun setShopType(shopType: ShopType) = mutex.withLock {
        _shopType = shopType
    }

    suspend fun setItem(item: ItemStack) = mutex.withLock {
        _item = item
    }

    suspend fun setQuantity(quantity: Int) = mutex.withLock {
        _quantity = quantity
    }

    suspend fun setPrice(price: Double) = mutex.withLock {
        _price = price
    }

    suspend fun setOpen(open: Boolean) = mutex.withLock {
        _open = open
    }

    suspend fun setSellLimit(sellLimit: Int) = mutex.withLock {
        _sellLimit = sellLimit
    }
}