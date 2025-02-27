package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterWrite
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.incendo.cloud.paper.util.sender.Source
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class Cooldown(override val plugin: Plop, private val cooldown: Duration): Addon {

    private val cooldowns: Cache<Player, Long> = Caffeine.newBuilder()
        .expireAfterWrite(cooldown)
        .asCache()

    fun cooldownTag(cooldown: Int): TagResolver {
        return Placeholder.component("cooldown", Component.text(cooldown))
    }

    private suspend fun checkCooldown(player: Player): Int? {
        val remaining = cooldowns.getIfPresent(player)
        if (remaining == null) {
            cooldowns.put(player, System.currentTimeMillis())
            return null
        }
        // Remaining seconds
        return (((cooldown.inWholeMilliseconds) + remaining - System.currentTimeMillis()) / 1000.0).roundToInt()
    }

    suspend fun bool(player: Player): Boolean {
        val cooldown = checkCooldown(player)
        if (cooldown != null) {
            player.sendMiniMessage("preview.cooldown", args = arrayOf(cooldownTag(cooldown)))
            return true
        }
        return false
    }
}