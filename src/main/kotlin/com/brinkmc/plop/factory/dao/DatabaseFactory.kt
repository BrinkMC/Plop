package com.brinkmc.plop.factory.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.factory.dto.Factory
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

internal class DatabaseFactory(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun loadFactory(id: UUID): Factory? = mutex.withLock {
        val json = DB.query("SELECT * FROM factories WHERE factory_id=?", id.toString()) ?: return null
        return if (json.next()) {
            gson.fromJson(json.getString("data"), Factory::class.java)
        } else {
            null
        }
    }

    suspend fun saveFactory(factory: Factory) = mutex.withLock {
        val id = factory.id
        val data = gson.toJson(factory)
        DB.update("INSERT INTO factories (factory_id, data) VALUES (?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)",
            id, data
        )
    }

    suspend fun deleteFactory(factory: Factory) = mutex.withLock {
        val id = factory.id
        DB.update("DELETE FROM factories WHERE factory_id=?", id)
    }
}