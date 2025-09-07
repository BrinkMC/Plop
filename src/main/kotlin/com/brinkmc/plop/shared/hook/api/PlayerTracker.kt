package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTracker(override val plugin: Plop): Addon, State, Listener {

    override suspend fun load() { }

    override suspend fun kill() {
        Bukkit.getOnlinePlayers().forEach {
            playerService.clearCache(it)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun join(event: PlayerJoinEvent) {
        val player = event.player
        playerService.clearCache(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun change(event: PlayerChangedWorldEvent) {
        val player = event.player
        playerService.clearCache(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun change(event: PlayerTeleportEvent) {
        val player = event.player
        playerService.clearCache(player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun remove(event: PlayerQuitEvent) {
        val player = event.player
        playerService.clearCache(player)
    }
}