package com.brinkmc.plop.shared.base

import com.brinkmc.plop.shared.dto.HologramWrapper
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import io.lumine.mythic.bukkit.utils.caffeine.cache.Caffeine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.UUID
import kotlin.compareTo
import kotlin.text.set

internal interface Display : Addon, State {

    val playerHolograms: Cache<UUID, List<HologramWrapper>>
    val tickDelay: Int
    val centreOffset: Double

    var rendering: Boolean
    var render: Job?

    suspend fun startRenderLoop() {
        if (render?.isActive == true) return // Already running
        rendering = true
        logger.info("Starting display render task")
        render = plugin.launch { plugin.asyncScope {
            delay(tickDelay.ticks + 20) // Initial delay to allow players to load in
            while (rendering) {
                server.onlinePlayers.forEach { player ->
                    val plotId = playerService.getPlotId(player.uniqueId) ?: return@forEach
                    render(player.uniqueId, plotId)
                }
                delay(tickDelay.ticks)
            }
        } }
    }

    suspend fun stopRenderLoop() {
        rendering = false
        render?.cancel()
        render = null
        logger.info("Stopping display render task")
    }

    suspend fun render(playerId: UUID, plotId: UUID) {
        val marker = findClosestMarker(playerId, plotId) ?: run {
            hideHolograms(playerId, )
            return
        }
        showHolograms(playerId, plotId, marker.first)
    }

    suspend fun findClosestMarker(playerId: UUID, plotId: UUID): Pair<Any, Double>?
    suspend fun getViewDistance(): Double

    // Default show/hide logic for multiple holograms
    suspend fun showHolograms(playerId: UUID, plotId: UUID, marker: Any) {
        val wrappers = buildHolograms(playerId, plotId, marker)
        hideHolograms(playerId) // Hide previous if any
        if (playerHolograms.getIfPresent(playerId) == wrappers) return // Already shown
        playerHolograms[playerId] = wrappers

        wrappers.forEach {
            playerService.showHologram(playerId, it.hologram)
        }

        doShowHologram(playerId, marker)
    }

    suspend fun hideHolograms(playerId: UUID) {
        val wrappers = playerHolograms.getIfPresent(playerId) ?: return
        playerHolograms.invalidate(playerId)

        wrappers.forEach {
            playerService.hideHologram(playerId, it.hologram)
        }

        doHideHologram(playerId, wrappers.first().id)
    }

    // Subclasses build all holograms for the marker
    suspend fun buildHolograms(playerId: UUID, plotId: UUID, marker: Any): List<HologramWrapper>

    // Overridable for custom show/hide behavior
    suspend fun doShowHologram(playerId: UUID, marker: Any) {}
    suspend fun doHideHologram(playerId: UUID, marker: Any) {}

    // Manual update API
    suspend fun updateHolograms(playerId: UUID, plotId: UUID) {
        val marker = findClosestMarker(playerId, plotId) ?: return
        showHolograms(playerId, plotId, marker)
    }
}