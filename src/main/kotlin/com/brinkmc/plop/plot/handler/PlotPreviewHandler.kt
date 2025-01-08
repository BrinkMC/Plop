package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.preview.PreviewInstance
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.stacksToBase64
import org.bukkit.Bukkit
import java.util.UUID

/*
Keep track of all active preview instances
Ensure that data is saved e.t.c
 */

class PlotPreviewHandler(override val plugin: Plop): Addon, State {

    override fun load() {

    }

    override fun kill() {
        previews.clear()
    }

    val previews = mutableListOf<PreviewInstance>()

    // All functions available to player
    fun startPreview(player: UUID) {

        val bukkitPlayer = Bukkit.getPlayer(player)

        if (bukkitPlayer == null) {
            logger.error("Failed to start preview, player doesn't exist")
            return
        }

        val previewInstance = PreviewInstance(
            plugin,
            player,
            bukkitPlayer.location.clone(),
            stacksToBase64(bukkitPlayer.inventory.contents)
        )
        // Add to the loaded instances
        previews.add(previewInstance)

    }

    fun endPreview(player: UUID) {
        val toRemove = previews.find { it.player == player } // Get preview

        if (toRemove == null) {
            logger.error("Can't seem to remove the preview")
            return
        }

        val bukkitPlayer = Bukkit.getOfflinePlayer(player)
        previews.remove(toRemove) // Remove from list

        bukkitPlayer.player?.teleport(toRemove.previousLocation) // Only if they end preview normally

        // Must also set their location
    }

    fun claimPlot(player: UUID){

    }

    fun nextPlot(player: UUID) {

    }

    fun previousPlot(player: UUID) {

    }

}