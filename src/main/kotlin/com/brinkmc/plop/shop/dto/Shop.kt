package com.brinkmc.plop.shop.dto

import com.brinkmc.plop.shop.constant.ShopType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.sql.Timestamp
import java.util.UUID

data class Shop(
    val id: UUID,
    val location: String,

    private var _shopType: ShopType,
    private var _item: ItemStack,
    private var _quantity: Int,
    private var _price: Double, // Sell price / buy price
    private var _open: Boolean,
    private var _transaction: MutableList<ShopTransaction>,

    // Only active if shopType is BUY
    private var _sellLimit: Int,
) {
    private val mutex = Mutex()

    // Thread-safe getters

    val shopType: ShopType get() = _shopType
    val item: ItemStack get() = _item.clone()
    val quantity: Int get() = _quantity
    val price: Double get() = _price
    val open: Boolean get() = _open
    val transactions: List<ShopTransaction> get() = _transaction.toList()

    val sellLimit: Int get() = _sellLimit

    // Thread-safe setters
    suspend fun setShopType(type: ShopType) = mutex.withLock {
        _shopType = type
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

    suspend fun setSellLimit(limit: Int) = mutex.withLock {
        _sellLimit = limit
    }

    suspend fun reduceLimit(amount: Int) = mutex.withLock {
        _sellLimit -= amount
        if (_sellLimit <= 0) {
            _sellLimit = 0
            close() // Auto close shop
        }
    }

    suspend fun open() = mutex.withLock {
        _open = true
    }

    suspend fun close() = mutex.withLock {
        _open = false
    }

    suspend fun addQuantity(amount: Int) = mutex.withLock {
        _quantity += amount
    }

    suspend fun removeQuantity(amount: Int) = mutex.withLock {
        _quantity -= amount
    }

    suspend fun addTransaction(playerId: UUID, amount: Int, cost: Double) = mutex.withLock {
        _transaction.add(ShopTransaction(Timestamp(System.currentTimeMillis()), playerId, amount, cost ))
    }
}

