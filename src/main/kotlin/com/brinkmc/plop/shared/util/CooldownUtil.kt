package com.brinkmc.plop.shared.util

import kotlin.time.Duration

//class Cooldown(override val plugin: Plop, private val cooldown: Duration): Addon {
//
//    private val cooldowns: Cache<Player, Long> = Caffeine.newBuilder()
//        .expireAfterWrite(cooldown)
//        .asCache()
//
//    fun cooldownTag(cooldown: Int): TagResolver {
//        return Placeholder.component("cooldown", Component.text(cooldown))
//    }
//
//    private suspend fun checkCooldown(player: Player): Int? {
//        val remaining = cooldowns.getIfPresent(player)
//        if (remaining == null) {
//            cooldowns.put(player, System.currentTimeMillis())
//            return null
//        }
//        // Remaining seconds
//        return (((cooldown.inWholeMilliseconds) + remaining - System.currentTimeMillis()) / 1000.0).roundToInt()
//    }
//
//    suspend fun bool(player: Player): Boolean {
//        val cooldown = checkCooldown(player)
//        if (cooldown != null) {
//            player.sendSound(SoundKey.CLICK)
//            player.sendMiniMessage(MessageKey.COOLDOWN, args = arrayOf(cooldownTag(cooldown)))
//            return true
//        }
//        return false
//    }
//}


private val cooldowns = mutableMapOf<Any, Long>()

fun <ID : Any> ID.cooldown(
    duration: Duration,
    onAllowed: () -> Unit,
    onCooldown: (remainingMillis: Long) -> Unit
) {
    val now = System.currentTimeMillis()
    val last = cooldowns[this] ?: 0L
    val elapsed = now - last
    if (elapsed >= duration.inWholeMilliseconds) {
        cooldowns[this] = now
        onAllowed()
    } else {
        onCooldown(duration.inWholeMilliseconds - elapsed)
    }
}

//usage:
//        uniqueId.cooldown(5.seconds,  {
//            aahhh
//        }) { remaining ->
//            aaaa
//        }