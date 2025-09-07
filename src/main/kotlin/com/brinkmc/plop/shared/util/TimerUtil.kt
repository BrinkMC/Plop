package com.brinkmc.plop.shared.util

import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.collections.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun withTimer(
    seconds: Int,
    onTick: suspend (secondsLeft: Int) -> Boolean // return true to break early
) {
    for (i in 0 until seconds) {
        val secondsLeft = seconds - i
        val shouldBreak = onTick(secondsLeft)
        if (shouldBreak) break
        delay(1.seconds)
    }
}

