package com.brinkmc.plop.shop.shop

import com.brinkmc.plop.shared.util.shop.ShopType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.sql.Timestamp
import java.util.UUID

data class Shop(
    private val _id: UUID,
    private val _location: Location,

    private var _shopType: ShopType,
    private var _item: ItemStack,
    private var _quantity: Int,
    private var _price: Float, // Sell price / buy price
    private var _open: Boolean,
    private var _transaction: MutableList<ShopTransaction>,

    // Only active if shopType is BUY
    private var _buyLimit: Int,
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val id: UUID get() = _id
    val location: Location get() = _location

    val shopType: ShopType get() = _shopType
    val item: ItemStack get() = _item.clone()
    val quantity: Int get() = _quantity
    val price: Float get() = _price
    val open: Boolean get() = _open
    val transactions: List<ShopTransaction> get() = _transaction.toList()

    val buyLimit: Int get() = _buyLimit

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

    suspend fun setPrice(price: Float) = mutex.withLock {
        _price = price
    }

    suspend fun setBuyLimit(limit: Int) = mutex.withLock {
        _buyLimit = limit
    }

    suspend fun reduceLimit(amount: Int) = mutex.withLock {
        _buyLimit -= amount
        if (_buyLimit <= 0) {
            _buyLimit = 0
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

    // Thread-safe snapshot
    suspend fun getSnapshot(): Shop = mutex.withLock {
        copy(
            _location = _location.clone(),
            _item = _item.clone()
        )
    }

    suspend fun addTransaction(playerId: UUID, amount: Int, type: ShopType) = mutex.withLock {
        _transaction.add(ShopTransaction(Timestamp(System.currentTimeMillis()), playerId, amount, type ))
    }
}

