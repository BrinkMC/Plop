package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.coroutines.CoroutineContext

// Credit to MCCoroutines for the code snippets

suspend fun <T> sync( // Synchronous function
    block: suspend CoroutineScope.() -> T // Block as in a block of code
): T =
    withContext(Dispatchers.sync, block) // Runs with coroutine context .sync
suspend fun <T> async(
    block: suspend CoroutineScope.() -> T // Block
): T =
    withContext(Dispatchers.async, block) // Runs with coroutine context .async


class MinecraftCoroutineDispatcher(private val plop: Plop) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        plop.launch { block.run() }
    }
}

private object DispatcherContainer {
    val sync: CoroutineDispatcher by lazy {
        MinecraftCoroutineDispatcher(JavaPlugin.getPlugin(Plop::class.java))
    }
}

val Dispatchers.async: CoroutineDispatcher
    get() = IO

val Dispatchers.sync: CoroutineDispatcher
    get() = DispatcherContainer.sync

val asyncScope = CoroutineScope(Dispatchers.async)
val syncScope = CoroutineScope(Dispatchers.sync)