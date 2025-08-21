package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.expireAfterWrite
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import kotlin.math.roundToInt
import kotlin.time.Duration

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
            player.sendSound(SoundKey.CLICK)
            player.sendMiniMessage(MessageKey.COOLDOWN, args = arrayOf(cooldownTag(cooldown)))
            return true
        }
        return false
    }
}