package com.brinkmc.plop.shop.storage.database

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.*
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.Funcs.toLocation
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class DatabaseShop(override val plugin: Plop): Addon, State {

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun load(shopId: UUID): Shop? {
        val shop = loadShopCore(shopId) ?: return null
        val location = loadShopLocation(shopId) ?: return null
        return shop.copy(location = location)
    }

    private suspend fun loadShopCore(shopId: UUID): Shop? {
        val rs = DB.query("SELECT * FROM shops WHERE shop_id=?", shopId.toString()) ?: return null
        return if (rs.next()) {
            Shop(
                shopId = shopId,
                plotId = UUID.fromString(rs.getString("plot_id")),
                plotType = PlotType.valueOf(rs.getString("plot_type")),
                location = Location(null, 0.0, 0.0, 0.0), // Placeholder, will be replaced
                shopType = ShopType.valueOf(rs.getString("shop_type")),
                ware = ItemStack.deserialize(rs.getString("ware")),
                stock = rs.getInt("stock"),
                price = rs.getFloat("price")
            )
        } else null
    }

    private suspend fun loadShopLocation(shopId: UUID): Location? {
        val rs = DB.query("SELECT * FROM shop_locations WHERE shop_id=?", shopId.toString()) ?: return null
        return if (rs.next()) {
            rs.getString("shop_location").toLocation()
        } else null
    }

    suspend fun create(shop: Shop) {
        DB.update(
            "INSERT INTO shops (shop_id, plot_id, shop_type, ware, stock, price) VALUES (?, ?, ?, ?, ?, ?)",
            shop.shopId.toString(),
            shop.plotId.toString(),
            shop.shopType.toString(),
            shop.ware.serialize().toString(),
            shop.stock,
            shop.price
        )
        DB.update(
            "INSERT INTO shop_locations (shop_id, shop_location) VALUES (?, ?)",
            shop.shopId.toString(),
            shop.location.toString()
        )
    }

    suspend fun save(shop: Shop) {
        DB.update(
            "INSERT INTO shops (shop_id, plot_id, shop_type, ware, stock, price) VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE plot_id = VALUES(plot_id), shop_type = VALUES(shop_type), ware = VALUES(ware), stock = VALUES(stock), price = VALUES(price)",
            shop.shopId.toString(),
            shop.plotId.toString(),
            shop.shopType.toString(),
            shop.ware.serialize().toString(),
            shop.stock,
            shop.price
        )
        DB.update(
            "INSERT INTO shop_locations (shop_id, shop_location) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE shop_location = VALUES(shop_location)",
            shop.shopId.toString(),
            shop.location.toString()
        )
    }
}

@Throws(IOException::class)
fun ItemStack.serialiseToString(): String {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
        }
    }
}

@Throws(IOException::class, ClassNotFoundException::class)
fun String.deserialiseToItemStack(): ItemStack {
    val data = Base64.getDecoder().decode(this)
    ByteArrayInputStream(data).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as ItemStack
        }
    }
}