package com.brinkmc.plop.shared.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeout
import kotlin.collections.remove

class DeferredRequest<K, V> {
    private val map = hashMapOf<K, CompletableDeferred<V>>()

    /**
     * Create a request for [key] without expecting a result. Returns true if successful.
     */
    suspend fun request(key: K, override: Boolean = false): Boolean {
        // Four scenarios: override and exists, override and not exists, not override and exists, not override and not exists
        // Only fail if not overriding and already exists
        if (!override && map.containsKey(key)) {
            return false
        }
        map[key] = CompletableDeferred()
        return true
    }

    suspend fun await(key: K, timeoutMillis: Long? = null): V? {
        return map[key]?.let { deferred ->
            if (timeoutMillis == null) deferred.await()
            else withTimeout(timeoutMillis) { deferred.await() }
        }
    }

    /**
     * Fulfill the request for [key] with [value]. Returns true if a pending request existed and
     * was completed by this call.
     */
    fun fulfill(key: K, value: V): Boolean {
        val d = map.remove(key) ?: return false
        return d.complete(value)
    }

    /**
     * Mark the request as failed with [throwable]. Returns true if a pending request existed.
     */
    fun fail(key: K, throwable: Throwable): Boolean {
        val d = map.remove(key) ?: return false
        return d.completeExceptionally(throwable)
    }

    /**
     * Cancel the pending request for [key]. Returns true if a pending request existed.
     */
    fun cancel(key: K): Boolean {
        val d = map.remove(key) ?: return false
        d.cancel()
        return true
    }

    /** Helpers */
    fun hasRequest(key: K): Boolean = map.containsKey(key)
    fun pendingCount(): Int = map.size
}