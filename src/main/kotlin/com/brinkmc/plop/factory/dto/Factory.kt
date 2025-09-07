package com.brinkmc.plop.factory.dto

import com.brinkmc.plop.factory.constant.FactoryRotation
import com.brinkmc.plop.factory.constant.FactoryType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class Factory(
    val id: UUID, // Mythic Mobs Sync?
    val mythicId: UUID,

    // Mythic Mobs
    private var _factoryType: FactoryType,
    private var _factoryRotation: FactoryRotation,

    private var _augments: List<Augment>, // two slots?
) {
    private val mutex = Mutex()

    val factoryType: FactoryType get() = _factoryType
    val factoryRotation: FactoryRotation get() = _factoryRotation
    val augments: List<Augment> get() = _augments

    suspend fun setFactoryType(factoryType: FactoryType) = mutex.withLock {
        _factoryType = factoryType
    }

    suspend fun setFactoryRotation(factoryRotation: FactoryRotation) = mutex.withLock {
        _factoryRotation = factoryRotation
    }

    suspend fun setAugments(augments: List<Augment>) = mutex.withLock {
        _augments = augments
    }
}