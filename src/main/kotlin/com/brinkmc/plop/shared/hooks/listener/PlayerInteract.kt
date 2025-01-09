package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerInteract(override val plugin: Plop): Addon, State, Listener {
    override fun load() {}

    override fun kill() {}

    @EventHandler
    fun onPlayerInteract(playerInteractEvent: PlayerInteractEvent) {

    }
}