package com.brinkmc.plop.shared.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.Shop
import com.github.shynixn.mccoroutine.bukkit.ticks
import eu.decentsoftware.holograms.api.DHAPI
import kotlinx.coroutines.delay
import org.bukkit.entity.Player

class NexusDisplay(override val plugin: Plop): Addon, State {

    val renderDelay = 2.ticks

    override suspend fun load() {
        plotConfig.nexusConfig.display.forEach {

        }

        renderTask()
    }

    override suspend fun kill() {
        renderCheck.clear()
    }

    suspend fun renderTask() {
        while (true) { asyncScope {
            for (player in server.onlinePlayers) {
                plugin.playerTracker.locations.getIfPresent(player)?.let {
                    render(player, it)
                }
            }
            delay(renderDelay)
        } }
    }

    suspend fun render(player: Player, plot: Plot) {
        val startLoc = plot.nexus.getClosest(player.location)?.add(0.5, 1.5, 0.5) // Get centred starting location of hologram
        if (startLoc?.distanceSquared(player.location) > plotConfig.nexusConfig.viewDistance) {
            return
        }
        DHAPI.createHologram("nexus-${player.uniqueId}", startLoc, plotConfig.nexusConfig.display)
    }
}