package com.brinkmc.plop.factory.dto

import com.brinkmc.plop.factory.constant.FactoryType
import com.brinkmc.plop.shared.constant.StringLocation
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import java.util.UUID

data class Factory(
    val id: UUID, // Mythic Mobs Sync?

    // Mythic Mobs
    private var _location: String,
    private var _factoryType: FactoryType,
) {
    private val mutex = Mutex()

    val factoryType: FactoryType get() = _factoryType
    val location: String get() = _location

    suspend fun setFactoryType(factoryType: FactoryType) = mutex.withLock {
        _factoryType = factoryType
    }

    suspend fun setLocation(location: String) = mutex.withLock {
        _location = location
    }
}