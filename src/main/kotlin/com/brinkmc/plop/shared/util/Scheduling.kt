package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.sun.org.apache.bcel.internal.classfile.JavaClass
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
    withContext(DispatcherContainer.sync, block) // Runs with coroutine context .sync
suspend fun <T> async(
    block: suspend CoroutineScope.() -> T // Block
): T =
    withContext(DispatcherContainer.async, block) // Runs with coroutine context .async


class MinecraftCoroutineDispatcher(private val plop: Plop) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            plop.server.scheduler.runTask(plop, block)
        }
    }
}

class AsyncCoroutineDispatcher(private val plop: Plop) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (Bukkit.isPrimaryThread()) {
            plop.server.scheduler.runTaskAsynchronously(plop, block)
        } else {
            block.run()
        }
    }
}

private object DispatcherContainer {
    val async: CoroutineContext by lazy {
        AsyncCoroutineDispatcher(JavaPlugin.getPlugin(Plop::class.java))
    }

    val sync: CoroutineContext by lazy {
        MinecraftCoroutineDispatcher(JavaPlugin.getPlugin(Plop::class.java))
    }
}

val Dispatchers.async: CoroutineContext
    get() =  DispatcherContainer.async

val Dispatchers.sync: CoroutineContext
    get() =  DispatcherContainer.sync

val asyncScope = CoroutineScope(Dispatchers.async)
val syncScope = CoroutineScope(Dispatchers.sync)