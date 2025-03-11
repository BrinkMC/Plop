package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythic.bukkit.events.MythicPlayerAttackEvent
import io.lumine.mythiccrucible.events.MythicFurniturePlaceEvent
import io.lumine.mythiccrucible.events.MythicFurnitureRemoveEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    // Duo of events to prevent attacks in plots
    @EventHandler
    suspend fun mythicPlayerAttackEvent(event: MythicPlayerAttackEvent) {
        // Prevent use of attacks inside plots unless it is your own
        val player = event.player.uniqueId.player() ?: return

        if (!player.world.isPlotWorld()) { // Ignore if not in plot world
            return
        }
        val playerPlot = player.getCurrentPlot()?.owner?.isPlayer(player) ?: return

        if (!playerPlot) { // Ignore if they're in own plot
            return
        }

        event.isCancelled = true
    }

    @EventHandler
    suspend fun entityDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) {
            return
        }

        val player = event.entity as Player

        if (!player.world.isPlotWorld()) { // Ignore if not in plot world
            return
        }
        val plot = player.getCurrentPlot() ?: return

        if (plot.owner.isPlayer(player)) { // Ignore if they're in own plot
            return
        }

        event.isCancelled = true
    }
}