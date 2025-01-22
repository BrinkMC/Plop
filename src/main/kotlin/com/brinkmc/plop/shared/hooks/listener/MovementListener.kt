package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.sync
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent

class MovementListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler
    suspend fun onPlayerTeleport(playerTeleportEvent: PlayerTeleportEvent) = sync {

        val worlds = plots.handler.getPlotWorlds()

        if (!worlds.contains(playerTeleportEvent.to.world) && !worlds.contains(playerTeleportEvent.from.world)) return@sync // Not applicable

        plots.handler.updateBorder(playerTeleportEvent.player.uniqueId)

        // Because it involves these worlds, we should do something about it


    }
}