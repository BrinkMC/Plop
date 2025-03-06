package com.brinkmc.plop.shop.shop

import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.hooks.Economy
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.sql.Timestamp
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
    private var _item: ItemStack,
    private var _quantity: Int,
    private var _sellPrice: Float,
    private var _buyPrice: Float,
    private var _buyLimit: Int,
    private var _open: Boolean,
    private var _transaction: MutableList<ShopTransaction>
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val location: Location get() = _location
    val item: ItemStack get() = _item.clone()
    val quantity: Int get() = _quantity
    val sellPrice: Float get() = _sellPrice
    val buyPrice: Float get() = _buyPrice
    val buyLimit: Int get() = _buyLimit
    val open: Boolean get() = _open
    val transactions: List<ShopTransaction> get() = _transaction.toList()

    // Get bukkit chest
    val chest: Chest
        get() {
            val block = _location.block
            return block as Chest
        }

    // Thread-safe setters
    suspend fun setLocation(location: Location) = mutex.withLock {
        _location = location
    }

    suspend fun setItem(item: ItemStack) = mutex.withLock {
        _item = item
    }

    suspend fun setQuantity(quantity: Int) = mutex.withLock {
        _quantity = quantity
    }

    suspend fun setSellPrice(price: Float) = mutex.withLock {
        _sellPrice = price
    }

    suspend fun setBuyPrice(price: Float) = mutex.withLock {
        _buyPrice = price
    }

    suspend fun setBuyLimit(limit: Int) = mutex.withLock {
        _buyLimit = limit
    }

    suspend fun setOpen(open: Boolean) = mutex.withLock {
        _open = open
    }

    // Thread-safe operations
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

    fun isSell(): Boolean {
        return sellPrice != -1.0f
    }

    fun isBuy(): Boolean {
        return buyPrice != -1.0f
    }

    suspend fun unsetSell() = mutex.withLock {
        _sellPrice = -1.0f
    }

    suspend fun unsetBuy() = mutex.withLock {
        _buyPrice = -1.0f
        _buyLimit = -1
    }

    suspend fun doTransaction(player: Player, amount: Int, type: ShopType, economy: Economy) = mutex.withLock {
        when (type) {
            ShopType.BUY -> {
                setQuantity(quantity - amount)
                setBuyLimit(buyLimit - amount)
                owner.depositBalance(economy, (amount * buyPrice).toDouble())
                economy.withdraw(player, (amount * buyPrice).toDouble())
            }
            ShopType.SELL -> {
                setQuantity(quantity + amount)
                owner.withdrawBalance(economy, (amount * sellPrice).toDouble())
                economy.deposit(player, (amount * sellPrice).toDouble())
            }
        }
        addTransaction(player.uniqueId, amount, type)
    }

    private suspend fun addTransaction(playerId: UUID, amount: Int, type: ShopType) = mutex.withLock {
        _transaction.add(ShopTransaction(playerId, amount, type, Timestamp(System.currentTimeMillis())))
    }

}

data class ShopTransaction(
    val playerId: UUID,
    val amount: Int,
    val type: ShopType,
    val timestamp: Timestamp
)