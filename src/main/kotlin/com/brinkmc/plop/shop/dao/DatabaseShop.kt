package com.brinkmc.plop.shop.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.brinkmc.plop.shop.dto.Shop
import com.brinkmc.plop.shop.dto.ShopTransaction
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

internal class DatabaseShop(override val plugin: Plop) : Addon, State {

    private val mutex = Mutex()

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun loadShop(id: UUID): Shop? = mutex.withLock {
        val json = DB.query("SELECT * FROM shops WHERE shop_id=?", id.toString()) ?: return null
        return if (json.next()) {
            gson.fromJson(json.getString("data"), Shop::class.java)
        } else {
            null
        }
    }

    suspend fun saveShop(shop: Shop) = mutex.withLock {
        val id = shop.id
        val data = gson.toJson(shop)
        DB.update(
            "INSERT INTO shops (shop_id, data) VALUES (?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)",
            id,
            data
        )
    }

    suspend fun deleteShop(shop: Shop) = mutex.withLock {
        val id = shop.id
        DB.update("DELETE FROM shops WHERE shop_id=?", id)
    }
}