package com.brinkmc.plop.shared.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player

class NexusDisplay(override val plugin: Plop): Addon, State {

    val nexusHolograms = Caffeine.newBuilder().asCache<Player, Hologram>()
    val renderDelay = 2.ticks

    override suspend fun load() {
        renderTask()
    }

    override suspend fun kill() { }

    private suspend fun renderTask() {
        while (true) { asyncScope {
            for (player in server.onlinePlayers) {
                plugin.playerTracker.locations.getIfPresent(player)?.let {
                    render(player, it)
                }
            }
            delay(renderDelay)
        } }
    }

    private suspend fun render(player: Player, plot: Plot) { // Get centred starting location of hologram
        val startLoc = plot.nexus.getClosest(player.location)?.add(0.5, 1.5, 0.5) ?: return // No nexus
        if (startLoc.distanceSquared(player.location) >= plotConfig.nexusConfig.viewDistance) { // Too far away
            far(player)
        }
        else {
            near(player, plot, startLoc)
        }
    }

    private suspend fun far(player: Player) {
        val potentialHologram = nexusHolograms.getIfPresent(player) ?: return // Is there a hologram?
        if (!potentialHologram.isVisible(player)) { // There is hologram check if visible
            return
        }
        potentialHologram.removeShowPlayer(player) // It shouldn't be visible anymore
    }

    private suspend fun near(player: Player, plot: Plot, location: Location) {
        val substitutedValues = plotConfig.nexusConfig.display.map {
            it.replace("<owner>", plot.owner.getName())
        }
        val potentialHologram = nexusHolograms.get(player) {
            val create = DHAPI.createHologram("nexus-${player.uniqueId}", location, false, substitutedValues)
            create.isDefaultVisibleState = false
            create
        }
        // Teleport the hologram to the new location (should be the same text e.t.c)
        potentialHologram.location = location
    }
}