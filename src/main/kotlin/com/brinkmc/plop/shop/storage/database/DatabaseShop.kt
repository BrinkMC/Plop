package com.brinkmc.plop.shop.storage.database

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.*
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.Funcs.fullString
import com.brinkmc.plop.shared.util.Funcs.toLocation
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.Timestamp
import java.util.*
import kotlin.toString

class DatabaseShop(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun load(shopId: UUID): Shop? = mutex.withLock {
        val id = shopId
        val shop = loadShopCore(id) ?: return null
        val location = loadShopLocation(id) ?: return null
        return shop.copy(_location = location)
    }

    private suspend fun loadShopCore(shopId: UUID): Shop? {
        val rs = DB.query("SELECT * FROM shops WHERE shop_id=?", shopId.toString()) ?: return null
        return if (rs.next()) {
            Shop(
                shopId = shopId,
                plotId = UUID.fromString(rs.getString("plot_id")),
                plotType = PlotType.valueOf(rs.getString("plot_type")),
                _location = Location(null, 0.0, 0.0, 0.0), // Temporary placeholder, will be replaced
                _shopType = ShopType.valueOf(rs.getString("shop_type")),
                _ware = ItemStack.deserializeBytes(rs.getBytes("ware")),
                _stock = rs.getInt("stock"),
                _stockLimit = rs.getInt("stock_limit"),
                _open = rs.getBoolean("open"),
                _price = rs.getFloat("price")
            )
        } else null
    }

    private suspend fun loadShopLocation(shopId: UUID): org.bukkit.Location? {
        val rs = DB.query("SELECT * FROM shop_locations WHERE shop_id=?", shopId.toString()) ?: return null
        return if (rs.next()) {
            rs.getString("shop_location").toLocation()
        } else null
    }

    // Transaction logic
    suspend fun loadTransactions(shopId: UUID): List<ShopTransaction> = mutex.withLock {
        val transactions = mutableListOf<ShopTransaction>()
        val rs = DB.query("SELECT * FROM shops_log WHERE shop_id=? ORDER BY trans_timestamp DESC", shopId.toString())

        while (rs?.next() == true) {
            transactions.add(
                ShopTransaction(
                    transId = rs.getInt("trans_id"),
                    shopId = UUID.fromString(rs.getString("shop_id")),
                    playerId = UUID.fromString(rs.getString("player_id")),
                    timestamp = rs.getTimestamp("trans_timestamp")
                )
            )
        }

        return transactions
    }

    suspend fun create(shop: Shop) = mutex.withLock {
        val id = shop.shopId.toString()
        DB.update(
            "INSERT INTO shops (shop_id, plot_id, plot_type, shop_type, ware, stock, stock_limit, open, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            id,
            shop.plotId.toString(),
            shop.plotType.toString(),
            shop.shopType.toString(),
            shop.ware.serializeAsBytes(),
            shop.stock,
            shop.stockLimit,
            shop.open,
            shop.price
        )
        DB.update(
            "INSERT INTO shop_locations (shop_id, shop_location) VALUES (?, ?)",
            id,
            shop.location.fullString()
        )
    }

    suspend fun save(unsafe: Shop) = mutex.withLock {
        val shop = unsafe.getSnapshot()
        val id = shop.shopId.toString()
        // Update shops table
        DB.update(
            "INSERT INTO shops (shop_id, plot_id, plot_type, shop_type, ware, stock, stock_limit, open, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id), plot_type = VALUES(plot_type), shop_type = VALUES(shop_type), " +
                    "ware = VALUES(ware), stock = VALUES(stock), stock_limit = VALUES(stock_limit), open = VALUES(open), price = VALUES(price)",
            id,
            shop.plotId.toString(),
            shop.plotType.toString(),
            shop.shopType.toString(),
            shop.ware.serializeAsBytes(),
            shop.stock,
            shop.stockLimit,
            shop.open,
            shop.price
        )

        // Update shop_locations table
        DB.update(
            "INSERT INTO shop_locations (shop_id, shop_location) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE shop_location = VALUES(shop_location)",
            id,
            shop.location.fullString()
        )
    }

    suspend fun delete(shop: Shop) = mutex.withLock {
        val id = shop.shopId.toString()
        // Delete from shops_log first (respect foreign key constraints)
        DB.update("DELETE FROM shops_log WHERE shop_id=?", id)
        // Delete from shop_locations
        DB.update("DELETE FROM shop_locations WHERE shop_id=?", id)
        // Finally delete from shops table
        DB.update("DELETE FROM shops WHERE shop_id=?", id)
    }

    suspend fun recordTransaction(shopId: UUID, playerId: UUID) = mutex.withLock {
        DB.update(
            "INSERT INTO shops_log (shop_id, player_id) VALUES (?, ?)",
            shopId.toString(),
            playerId.toString()
        )
    }

    suspend fun getShopsByPlotId(plotId: UUID): List<Shop> {
        val shops = mutableListOf<Shop>()
        val rs = DB.query("SELECT shop_id FROM shops WHERE plot_id=?", plotId.toString())

        while (rs?.next() == true) {
            val shopId = UUID.fromString(rs.getString("shop_id"))
            load(shopId)?.let { shops.add(it) }
        }

        return shops
    }
}

data class ShopTransaction(
    val transId: Int,
    val shopId: UUID,
    val playerId: UUID,
    val timestamp: Timestamp
)