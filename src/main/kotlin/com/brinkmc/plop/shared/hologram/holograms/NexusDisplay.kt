package com.brinkmc.plop.shared.hologram.holograms

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Display
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.dto.HologramWrapper
import com.brinkmc.plop.shared.hook.api.FancyHologram
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.UUID


class NexusDisplay(override val plugin: Plop) : Display {

    override val playerHolograms = Caffeine.newBuilder().asCache<UUID, List<HologramWrapper>>()
    override val tickDelay = 3
    override val centreOffset = 0.5

    override var rendering: Boolean = true
    override var render: Job? = null

    override suspend fun findClosestMarker(playerId: UUID, plotId: UUID): Pair<Any, Double>? {
        val playerLocation = playerService.getLocation(playerId) ?: return null
        val nexi: List<String> = plotNexusService.getNexi(plotId)

        return nexi.mapNotNull { nexusId ->
            val nexusLocation = nexusId.toLocation() ?: return@mapNotNull null
            val distance = nexusLocation.add(centreOffset, 0.0, centreOffset).distanceSquared(playerLocation)
            if (distance < configService.plotConfig.nexusConfig.viewDistance) {
                nexusLocation to distance
            } else null
        }.minByOrNull { it.second }
    }

    override suspend fun getViewDistance(): Double = configService.plotConfig.nexusConfig.viewDistance.toDouble()

    override suspend fun buildHolograms(playerId: UUID, plotId: UUID, marker: Any): List<HologramWrapper> {
        val nexusId = marker as? String ?: return emptyList()

        val lines = configService.plotConfig.nexusConfig.display
        val offset = lines.lastOrNull()?.substringBefore(":")?.toDoubleOrNull() ?: 0.0
        val scaleNumber = lines.lastOrNull()?.substringAfter(":")?.toFloatOrNull() ?: 1.0f
        val scale = Vector3f(scaleNumber, scaleNumber, scaleNumber)

        val location = nexusId.toLocation()?.apply { add(centreOffset, offset, centreOffset) }
            ?: return emptyList()

        val text: List<String> = lines.dropLast(1).map { messages.resolveTags(it, playerId, plotId) }
        val data = TextHologramData("nexus-$nexusId", location).apply { this.text = text }

        val hologram = hologramService.createTextHologram(data)
        return listOf(HologramWrapper(plotId.toString(), hologram, offset, scale))
    }

    override suspend fun load() {
        startRenderLoop()
    }

    override suspend fun kill() {
        stopRenderLoop()
    }
}

//class NexusDisplay(override val plugin: Plop): Addon, State {
//    companion object {
//        // Hologram positioning constants
//        private const val HOLOGRAM_HEIGHT_OFFSET = 1.5
//        private const val CENTER_OFFSET = 0.5
//        private const val TICK_DELAY = 3
//    }
//
//    // Track which hologram location is currently shown to each player
//    private val activeHolograms = Caffeine.newBuilder().asCache<Player, Location?>()
//
//    // Store hologram instances by player
//    private val playerHolograms = Caffeine.newBuilder().asCache<Player, Hologram>()
//
//    private val hologramManager: FancyHologram get() = plugin.hookService.display
//
//    override suspend fun load() {
//        plugin.async { startRenderLoop() }
//    }
//
//    override suspend fun kill() { }
//
//    private suspend fun startRenderLoop() {
//        logger.info("Starting nexus hologram render task")
//        while (true) {
//            server.onlinePlayers.forEach { player ->
//                val plot = playerTracker.locations.get(player) ?: return@forEach
//                updateHologramForPlayer(player, plot)
//            }
//            delay(TICK_DELAY.ticks)
//        }
//    }
//
//    private suspend fun updateHologramForPlayer(player: Player, plot: Plot) {
//        // Find the closest nexus and calculate centered position
//        val nexusLocation = findClosestNexusPosition(player, plot) ?: return
//
//        // Handle visibility based on distance
//        if (isPlayerTooFarFromNexus(player, nexusLocation)) {
//            hideHologram(player)
//        } else {
//            showHologram(player, plot, nexusLocation)
//        }
//    }
//
//    private suspend fun findClosestNexusPosition(player: Player, plot: Plot): Location? {
//        return plot.nexus.getClosest(player.location)?.clone()?.apply {
//            add(CENTER_OFFSET, HOLOGRAM_HEIGHT_OFFSET, CENTER_OFFSET)
//        }
//    }
//
//    private fun isPlayerTooFarFromNexus(player: Player, location: Location): Boolean {
//        return location.distanceSquared(player.location) >= plotConfig.nexusConfig.viewDistance
//    }
//
//    private suspend fun hideHologram(player: Player) {
//        // Skip if already hidden
//        if (activeHolograms.getIfPresent(player) == null) return
//
//        // Mark as inactive and remove
//        activeHolograms[player] = null
//        logger.debug("Hiding nexus hologram for ${player.name}")
//
//        val hologram = playerHolograms.getIfPresent(player) ?: return
//        hologramManager.hideHologram(player, hologram)
//    }
//
//    private suspend fun showHologram(player: Player, plot: Plot, location: Location) {
//        // Skip if already showing at this location
//        if (activeHolograms.getIfPresent(player) == location) return
//
//        logger.debug("Showing nexus hologram for ${player.name}")
//        activeHolograms[player] = location
//
//        // Get display text with personalized tags
//        val displayText = getDisplayTextForPlayer(player, plot)
//
//        // Get or create hologram and update it
//        val hologram = getOrCreateHologram(player, location, displayText)
//        updateHologramPosition(hologram, location)
//        hologramManager.showHologram(player, hologram)
//    }
//
//    private fun getDisplayTextForPlayer(player: Player, plot: Plot): List<String> {
//        val tags = lang.getTags(player = player, plot = plot)
//        return plotConfig.nexusConfig.display.map { lang.resolveTags(it, tags) }
//    }
//
//    private suspend fun getOrCreateHologram(player: Player, location: Location, text: List<String>): Hologram {
//        return playerHolograms.get(player) {
//            val hologramData = TextHologramData("nexus-${player.uniqueId}", location)
//            hologramData.text = text
//            hologramManager.createHologram(hologramData)
//        }
//    }
//
//    private fun updateHologramPosition(hologram: Hologram, location: Location) {
//        hologram.data.setLocation(location)
//    }
//}