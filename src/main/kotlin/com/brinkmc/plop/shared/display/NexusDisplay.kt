package com.brinkmc.plop.shared.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Display
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import kotlin.text.compareTo
import kotlin.text.get
import kotlin.text.set

class NexusDisplay(override val plugin: Plop): Addon, State {
    companion object {
        // Hologram positioning constants
        private const val HOLOGRAM_HEIGHT_OFFSET = 1.5
        private const val CENTER_OFFSET = 0.5
        private const val TICK_DELAY = 3
    }

    // Track which hologram location is currently shown to each player
    private val activeHolograms = Caffeine.newBuilder().asCache<Player, Location?>()

    // Store hologram instances by player
    private val playerHolograms = Caffeine.newBuilder().asCache<Player, Hologram>()

    private val hologramManager: Display get() = plugin.hooks.display

    override suspend fun load() {
        plugin.async { startRenderLoop() }
    }

    override suspend fun kill() { }

    private suspend fun startRenderLoop() {
        logger.info("Starting nexus hologram render task")
        while (true) {
            for (player in server.onlinePlayers) {
                val plot = plugin.playerTracker.locations.get(player) ?: continue
                updateHologramForPlayer(player, plot)
            }
            delay(TICK_DELAY.ticks)
        }
    }

    private suspend fun updateHologramForPlayer(player: Player, plot: Plot) {
        // Find closest nexus and calculate centered position
        val nexusLocation = findClosestNexusPosition(player, plot) ?: return

        // Handle visibility based on distance
        if (isPlayerTooFarFromNexus(player, nexusLocation)) {
            hideHologram(player)
        } else {
            showHologram(player, plot, nexusLocation)
        }
    }

    private suspend fun findClosestNexusPosition(player: Player, plot: Plot): Location? {
        return plot.nexus.getClosest(player.location)?.clone()?.apply {
            add(CENTER_OFFSET, HOLOGRAM_HEIGHT_OFFSET, CENTER_OFFSET)
        }
    }

    private fun isPlayerTooFarFromNexus(player: Player, location: Location): Boolean {
        return location.distanceSquared(player.location) >= plotConfig.nexusConfig.viewDistance
    }

    private suspend fun hideHologram(player: Player) {
        // Skip if already hidden
        if (activeHolograms.getIfPresent(player) == null) return

        // Mark as inactive and remove
        activeHolograms[player] = null
        logger.debug("Hiding nexus hologram for ${player.name}")

        val hologram = playerHolograms.getIfPresent(player) ?: return
        hologramManager.hideHologram(player, hologram)
    }

    private suspend fun showHologram(player: Player, plot: Plot, location: Location) {
        // Skip if already showing at this location
        if (activeHolograms.getIfPresent(player) == location) return

        logger.debug("Showing nexus hologram for ${player.name}")
        activeHolograms[player] = location

        // Get display text with personalized tags
        val displayText = getDisplayTextForPlayer(player, plot)

        // Get or create hologram and update it
        val hologram = getOrCreateHologram(player, location, displayText)
        updateHologramPosition(hologram, location)
        hologramManager.showHologram(player, hologram)
    }

    private fun getDisplayTextForPlayer(player: Player, plot: Plot): List<String> {
        val tags = lang.getTags(player = player, plot = plot)
        return plotConfig.nexusConfig.display.map { lang.resolveTags(it, tags) }
    }

    private suspend fun getOrCreateHologram(player: Player, location: Location, text: List<String>): Hologram {
        return playerHolograms.get(player) {
            val hologramData = TextHologramData("nexus-${player.uniqueId}", location)
            hologramData.text = text
            hologramManager.createHologram(hologramData)
        }
    }

    private fun updateHologramPosition(hologram: Hologram, location: Location) {
        hologram.data.setLocation(location)
    }
}