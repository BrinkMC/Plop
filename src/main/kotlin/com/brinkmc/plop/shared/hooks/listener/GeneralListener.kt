package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler
    suspend fun onPlayerJoin(playerJoinEvent: PlayerJoinEvent) {
        playerJoinEvent.player.personalPlot() // Populate the cache when they join so that first command isn't so slow
        playerJoinEvent.player.guildPlot()
        playerJoinEvent.player.updateBorder()
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