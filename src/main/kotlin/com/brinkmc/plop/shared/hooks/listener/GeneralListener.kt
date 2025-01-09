package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class GeneralListener(override val plugin: Plop): Addon, State, Listener {
    override fun load() {}

    override fun kill() {}

    @EventHandler
    fun onPlayerQuit(playerQuitEvent: PlayerQuitEvent) {
        onPlayerQuitPotentialPreview(playerQuitEvent)
    }

    private fun onPlayerQuitPotentialPreview(playerQuitEvent: PlayerQuitEvent) {
        val potentialPreview = plots.plotPreviewHandler.previews.find { it.player == playerQuitEvent.player.uniqueId }

        if (potentialPreview == null) { // Check is in preview
            return
        }

        playerQuitEvent.player.teleport(potentialPreview.previousLocation) // Teleport player back if they try to quit game while in preview
    }


}