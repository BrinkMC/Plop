package com.brinkmc.plop.shop.shop

import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import java.util.UUID

enum class ShopType { // Types of shop
    BUY,
    SELL
}

data class Shop(
    val shopId: UUID,
    val plotId: UUID,
    val plotType: PlotType,
    private var _location: Location,
    private var _shopType: ShopType,
    private var _ware: ItemStack,
    private var _stock: Int,
    private var _stockLimit: Int,
    private var _open: Boolean,
    private var _price: Float
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val location: Location get() = _location
    val shopType: ShopType get() = _shopType
    val ware: ItemStack get() = _ware.clone()
    val stock: Int get() = _stock
    val stockLimit: Int get() = _stockLimit
    val open: Boolean get() = _open
    val price: Float get() = _price

    // Thread-safe setters
    suspend fun setLocation(location: Location) = mutex.withLock {
        _location = location
    }

    suspend fun setShopType(type: ShopType) = mutex.withLock {
        _shopType = type
    }

    suspend fun setWare(ware: ItemStack) = mutex.withLock {
        _ware = ware.clone()
    }

    suspend fun setStock(stock: Int) = mutex.withLock {
        _stock = stock
    }

    suspend fun setStockLimit(limit: Int) = mutex.withLock {
        _stockLimit = limit
    }

    suspend fun setOpen(open: Boolean) = mutex.withLock {
        _open = open
    }

    suspend fun setPrice(price: Float) = mutex.withLock {
        _price = price
    }

    // Thread-safe operations
    suspend fun addStock(amount: Int) = mutex.withLock {
        _stock += amount
    }

    suspend fun removeStock(amount: Int) = mutex.withLock {
        _stock -= amount
    }

    // Thread-safe snapshot
    suspend fun getSnapshot(): Shop = mutex.withLock {
        copy(
            _location = _location.clone(),
            _ware = _ware.clone()
        )
    }

    val owner: PlotOwner by lazy {
        if (plotType == PlotType.GUILD) {
            val guild = Guilds.getApi().getGuild(plotId) // Try to find guild

            if (guild != null) {
                PlotOwner.GuildOwner(guild)
            } else {
                throw IllegalStateException("Guild not found for plot $plotId")
            }
        } else {
            PlotOwner.PlayerOwner(Bukkit.getOfflinePlayer(plotId))
        }
    }
}