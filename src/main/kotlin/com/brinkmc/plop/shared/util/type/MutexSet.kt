package com.brinkmc.plop.shared.util.type

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MutexSet<T> {
    private val set = mutableSetOf<T>()
    private val mutex = Mutex()

    suspend fun add(element: T): Boolean = mutex.withLock {
        set.add(element)
    }

    suspend fun remove(element: T): Boolean = mutex.withLock {
        set.remove(element)
    }

    suspend fun contains(element: T): Boolean = mutex.withLock {
        set.contains(element)
    }

    suspend fun toList(): List<T> = mutex.withLock {
        set.toList()
    }

    suspend fun size(): Int = mutex.withLock {
        set.size
    }
}