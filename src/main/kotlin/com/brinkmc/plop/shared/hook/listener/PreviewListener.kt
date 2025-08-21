package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import kotlin.time.Duration.Companion.milliseconds

class PreviewListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler
    suspend fun updateWorldBorder(event: PlayerTeleportEvent) {
        delay(10.milliseconds)
        event.player.updateBorder()
    }

    @EventHandler
    suspend fun updateWorldBorder(event: PlayerDeathEvent) {
        delay(10.milliseconds)
        event.player.updateBorder()
    }

    @EventHandler
    suspend fun preventPreviewDamage(event: EntityDamageEvent) {
        val player = if (event.entity is Player) {
            event.entity as Player
        } else { return }

        if (plots.previewHandler.getPreview(player.uniqueId) == null) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    suspend fun preventPreviewInteraction(event: PlayerInteractEvent) {
        val player = event.player

        if (plots.previewHandler.getPreview(player.uniqueId) == null) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    suspend fun preventPreviewItemPickup(event: EntityPickupItemEvent) {
        val player = if (event.entity is Player) {
            event.entity as Player
        } else { return }

        if (plots.previewHandler.getPreview(player.uniqueId) == null) {
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    suspend fun onPlayerQuit(playerQuitEvent: PlayerQuitEvent) {
        val potentialPreview = plots.previewHandler.getPreview(playerQuitEvent.player.uniqueId)

        if (potentialPreview == null) { // Check is in preview
            return
        }

        plots.previewHandler.endPreview(playerQuitEvent.player.uniqueId) // Remove
    }
}