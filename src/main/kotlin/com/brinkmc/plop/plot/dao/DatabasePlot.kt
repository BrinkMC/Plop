package com.brinkmc.plop.plot.dao

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

internal class DatabasePlot(override val plugin: Plop): Addon, State {

    private val mutex = Mutex()

    override suspend fun load() {}

    override suspend fun kill() {}

    suspend fun loadPlot(id: UUID): Plot? = mutex.withLock {
        val json = DB.query("SELECT * FROM plots WHERE plot_id=?", id.toString()) ?: return null
        return if (json.next()) {
            gson.fromJson(json.getString("data"), Plot::class.java)
        } else {
            null
        }
    }

    suspend fun savePlot(plot: Plot) = mutex.withLock {
        val id = plot.id
        val data = gson.toJson(plot)
        DB.update("INSERT INTO plots (plot_id, data) VALUES (?,?) ON DUPLICATE KEY UPDATE data=VALUES(data)",
            id, data
        )
    }

    suspend fun deletePlot(plot: Plot) = mutex.withLock {
        val id = plot.id
        DB.update("DELETE FROM plots WHERE plot_id=?", id)
    }
}

