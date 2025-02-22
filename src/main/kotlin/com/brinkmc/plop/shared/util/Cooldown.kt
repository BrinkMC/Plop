package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterWrite
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class Cooldown(override val plugin: Plop, val cooldownLength: Long): Addon {

    val cooldowns: Cache<Player, Long> = Caffeine.newBuilder()
        .expireAfterWrite(cooldownLength.seconds)
        .asCache<Player, Long>()

    suspend fun checkCooldown(player: Player): Int? {
        var cooldown = cooldowns.getIfPresent(player)
        if (cooldown == null) {
            cooldowns.put(player, System.currentTimeMillis())
            return null
        }
        // Remaining seconds
        return (((cooldownLength*1000) + cooldown - System.currentTimeMillis()) / 1000.0).roundToInt()
    }

    suspend fun bool(player: Player): Boolean {
        val cooldown = checkCooldown(player)
        if (cooldown != null) {
            player.sendMiniMessage(lang.get("preview.cooldown", null, cooldown))
            return true
        }
        return false
    }
}