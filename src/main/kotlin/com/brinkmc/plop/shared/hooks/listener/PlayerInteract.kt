package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.nexus.NexusManager
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.Material
import org.bukkit.block.Lectern
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerInteract(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler
    suspend fun preventNexusLeaving(event: PlayerChangedWorldEvent) {
        val player = event.player

        if (player.world.isPlotWorld()) {
            return
        }

        if (player.inventory.contains(plots.nexusManager.NEXUS_BOOK)) {
            player.inventory.removeAll { it == plots.nexusManager.NEXUS_BOOK } // NO NEXUS BOOKS ALLOWED OUTSIDE PLOT!!
        }
    }

    @EventHandler
    suspend fun onNexus(event: PlayerInteractEvent) {
        val block = event.clickedBlock

        if (block == null) {
            return
        }

        if (block.type != Material.LECTERN) {
            return
        }

        event.isCancelled = true

        val lectern = block.state as Lectern

        if (!lectern.inventory.contains(Material.WRITTEN_BOOK)) {
            event.isCancelled = false
            return
        }

        if (lectern.inventory.getItem(0)?.itemMeta?.displayName() != plots.nexusManager.NEXUS_BOOK.itemMeta.displayName()) {
            event.isCancelled = false
            return
        }

        // Definitely a nexus now

        val player = event.player

        if (!player.world.isPlotWorld()) {
            player.sendMiniMessage("land.not-in-plot-world")
            event.isCancelled = false
            return
        }

        val plot = player.getCurrentPlot()

        if (plot == null) {
            player.sendMiniMessage("land.not-in-plot")
            event.isCancelled = false
            return
        }

        if (!player.getPlots().contains(player.getCurrentPlot())) {
            player.sendMiniMessage("land.not-plot-owner")
            event.isCancelled = false
            return
        }

        if (!player.hasPermission("plop.nexus.use")) {
            player.sendMiniMessage("land.nexus-no-permission")
            event.isCancelled = false
            return
        }

        // Has permission to be here and use the nexus good

        if (event.action == Action.RIGHT_CLICK_BLOCK && event.player.isSneaking) {
            syncScope {
                lectern.inventory.clear() // Clear the lectern of the book
                // Drop book on ground
                block.world.dropItemNaturally(block.location, plots.nexusManager.NEXUS_BOOK)
            }
            return
        }

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            plugin.menus.nexusMainMenu.open(player)
            return
        }

        if (event.action == Action.LEFT_CLICK_BLOCK) {
            plugin.menus.nexusMainMenu.open(player)
            return
        }
    }
}