package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class VisitListener(override val plugin: Plop): Addon, State, Listener {

    override suspend fun load() { }

    override suspend fun kill() { }


    // This code was NOT worth the spaghetti of nested if statements
    @EventHandler(priority = EventPriority.NORMAL)
    suspend fun change(event: PlayerTeleportEvent) {
        val player = event.player
        val previousPlot = event.from.getCurrentPlot()
        val currentPlot = event.to.getCurrentPlot()

        // Skip if no plot transition (neither location is in a plot)
        if (previousPlot == null && currentPlot == null) {
            return
        }

        // Handle leaving a plot
        if (previousPlot != null && !isPlotOwner(previousPlot, player)) {
            decrementVisitorCount(previousPlot)
        }

        // Handle entering a plot
        if (currentPlot != null && !isPlotOwner(currentPlot, player)) {
            if (!canPlayerEnterPlot(currentPlot, player, event)) {
                return
            }
            incrementVisitorCount(currentPlot)
        }
    }

    private fun isPlotOwner(plot: Plot, player: Player): Boolean {
        return plot.owner.isPlayer(player)
    }

    private fun decrementVisitorCount(plot: Plot) {
        plot.visit.currentVisits -= 1
    }

    private fun incrementVisitorCount(plot: Plot) {
        plot.visit.currentVisits += 1
    }

    private fun canPlayerEnterPlot(plot: Plot, player: Player, event: PlayerTeleportEvent): Boolean {
        if (plot.visit.currentVisits >= plot.visit.limit) {
            player.sendMiniMessage(MessageKey.PLOT_FULL)
            player.sendSound(SoundKey.FAILURE)
            event.isCancelled = true
            return false
        }
        return true
    }
}