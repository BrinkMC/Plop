package com.brinkmc.plop.shop.dto

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class ShopAccess(
    val id: UUID,

    // Buying/Selling things
    private var _multiplier: Int?,
    private var _total: Int?,
) {
    private val mutex = Mutex()

    // Thread-safe getters
    val multiplier: Int? get() = _multiplier
    val total: Int? get() = _total

    suspend fun cycleMultiplier() = mutex.withLock {
        _multiplier = when (multiplier) {
            1 -> 10
            10 -> 100
            100 -> 1000
            1000 -> 1
            else -> 1
        }
    }

    suspend fun setTotal(total: Int) = mutex.withLock {
        _total = total
    }
}