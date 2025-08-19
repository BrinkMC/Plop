package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import com.sksamuel.aedile.core.asCache
import com.sksamuel.aedile.core.asLoadingCache
import org.bukkit.WorldBorder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTracker(override val plugin: Plop): Addon, State, Listener {

    val locations = Caffeine.newBuilder().asLoadingCache<Player, Plot?> {
        plots.handler.getPlotFromLocation(it.location)
    }

    override suspend fun load() { }

    override suspend fun kill() {
        locations.invalidateAll()
    }

    @EventHandler(priority = EventPriority.LOW)
    suspend fun join(event: PlayerJoinEvent) {
        val player = event.player
        locations.invalidate(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun change(event: PlayerChangedWorldEvent) {
        val player = event.player
        locations.invalidate(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun change(event: PlayerTeleportEvent) {
        val player = event.player
        locations.invalidate(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun remove(event: PlayerQuitEvent) {
        val player = event.player
        locations.invalidate(player)
    }

    suspend fun refresh(player: Player) {
        locations.invalidate(player)
    }
}