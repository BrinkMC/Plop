package com.brinkmc.plop.shared.util

import kotlinx.coroutines.delay
import org.bukkit.entity.Player
import kotlin.collections.get
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

sealed class TimerResult {
    object Success : TimerResult()
    data class Interrupted(val secondsLeft: Int) : TimerResult()
}

suspend fun withTimer(
    seconds: Int,
    onTick: suspend (secondsLeft: Int) -> Boolean // return true to break early
): TimerResult {
    for (i in 0 until seconds) {
        val secondsLeft = seconds - i
        val shouldBreak = onTick(secondsLeft)
        if (shouldBreak) return TimerResult.Interrupted(secondsLeft)
        delay(1.seconds)
    }
    return TimerResult.Success
}

inline fun <R> TimerResult.fold(
    onSuccess: () -> R,
    onInterrupted: (secondsLeft: Int) -> R
): R = when (this) {
    is TimerResult.Success -> onSuccess()
    is TimerResult.Interrupted -> onInterrupted(this.secondsLeft)
}