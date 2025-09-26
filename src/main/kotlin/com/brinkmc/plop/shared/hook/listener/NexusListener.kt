package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.SoundKey
import io.papermc.paper.event.player.PlayerInsertLecternBookEvent
import org.bukkit.Material
import org.bukkit.block.Lectern
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent

class NexusListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun detect(event: PlayerInsertLecternBookEvent) {
        val lectern = event.lectern
        val book = event.book

        if (book.itemMeta.displayName() != plots.nexusService.NEXUS_BOOK.itemMeta.displayName()) {
            return
        }

        if (!lectern.world.isPlotWorld()) {
            return
        }

        val plot = lectern.location.getCurrentPlot()

        logger.info("Adding nexus to plot ${plot?.plotId}")
        // Keep track that the nexus was placed
        plot?.addNexus(lectern.block.location)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    suspend fun destroy(event: BlockBreakEvent) {
        val block = event.block

        if (block.type != Material.LECTERN) {
            return
        }

        val lectern = block.state as Lectern

        if (!lectern.inventory.contains(Material.WRITTEN_BOOK)) {
            return
        }

        if (lectern.inventory.getItem(0)?.itemMeta?.displayName() != plots.nexusService.NEXUS_BOOK.itemMeta.displayName()) {
            return
        }

        // Definitely a nexus now

        val player = event.player

        val plot = player.getCurrentPlot() ?: return

        // Keep track that the nexus was destroyed
        plot.removeNexus(lectern.block.location)
    }

    @EventHandler
    suspend fun preventNexusLeaving(event: PlayerChangedWorldEvent) {
        val player = event.player

        if (player.world.isPlotWorld()) {
            return
        }

        if (player.inventory.contains(plots.nexusService.NEXUS_BOOK)) {
            player.inventory.removeAll { it == plots.nexusService.NEXUS_BOOK } // NO NEXUS BOOKS ALLOWED OUTSIDE PLOT!!
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    suspend fun onNexus(event: PlayerInteractEvent) {
//        val block = event.clickedBlock ?: return
//
//        if (block.type != Material.LECTERN) {
//            return
//        }
//
//        event.isCancelled = true
//
//        val lectern = block.state as Lectern
//
//        if (!lectern.inventory.contains(Material.WRITTEN_BOOK)) {
//            event.isCancelled = false
//            return
//        }
//
//        if (lectern.inventory.getItem(0)?.itemMeta?.displayName() != plots.nexusService.NEXUS_BOOK.itemMeta.displayName()) {
//            event.isCancelled = false
//            return
//        }
//
//        // Definitely a nexus now
//
//        val player = event.player
//
//        if (!player.world.isPlotWorld()) {
//            player.sendMiniMessage(MessageKey.NOT_PLOT)
//            event.isCancelled = false
//            return
//        }
//
//        val plot = player.getCurrentPlot()
//
//        if (plot == null) {
//            player.sendMiniMessage(MessageKey.NOT_PLOT)
//            player.sendSound(SoundKey.FAILURE)
//            event.isCancelled = false
//            return
//        }
//
//        if (!plot.owner.isPlayer(player)) {
//            player.sendMiniMessage(MessageKey.NOT_OWNER)
//            player.sendSound(SoundKey.FAILURE)
//            event.isCancelled = false
//            return
//        }
//
//        if (!player.hasPermission("plop.nexus.use")) {
//            player.sendMiniMessage(MessageKey.NO_PERMISSION)
//            player.sendSound(SoundKey.FAILURE)
//            event.isCancelled = false
//            return
//        }
//
//        // Has permission to be here and use the nexus good
//
//        if (event.action == Action.RIGHT_CLICK_BLOCK && event.player.isSneaking) {
//            syncScope {
//                lectern.inventory.clear() // Clear the lectern of the book
//                // Drop book on ground
//                block.world.dropItemNaturally(block.location, plots.nexusService.NEXUS_BOOK)
//            }
//            plot.removeNexus(lectern.block.location)
//            player.sendSound(SoundKey.CLICK)
//            return
//        }
//
//        if (event.action == Action.RIGHT_CLICK_BLOCK) {
//            plugin.menus.nexusMainMenu.open(player)
//            player.sendSound(SoundKey.CLICK)
//            return
//        }
//
//        if (event.action == Action.LEFT_CLICK_BLOCK) {
//            plugin.menus.nexusMainMenu.open(player)
//            player.sendSound(SoundKey.CLICK)
//            return
//        }
    }
}