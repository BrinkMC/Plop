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

class MythicListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler // Handle nexus placement + totem placement and registering in system
    suspend fun onMythicFurniturePlace(mythicFurniturePlaceEvent: MythicFurniturePlaceEvent) {

    }

    @EventHandler
    suspend fun onMythicFurnitureRemove(mythicFurnitureRemoveEvent: MythicFurnitureRemoveEvent) {

    }

    // Duo of events to prevent attacks in plots
    @EventHandler
    suspend fun mythicPlayerAttackEvent(event: MythicPlayerAttackEvent) {
        // Prevent use of attacks inside plots unless it is your own
        val player = Bukkit.getPlayer(event.player.uniqueId) ?: return

        if (!plots.handler.getPlotWorlds().contains(player.world)) { // Ignore if not in plot world
            return
        }

        if (!player.getPlots().contains(player.getCurrentPlot())) { // Ignore if they're in own plot
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

        if (!plots.handler.getPlotWorlds().contains(player.world)) { // Ignore if not in plot world
            return
        }

        if (player.getPlots().contains(player.getCurrentPlot())) { // Ignore if they're in own plot
            return
        }

        event.isCancelled = true
    }
}