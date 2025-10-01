package com.brinkmc.plop.shared.base

import com.brinkmc.plop.shared.dto.HologramWrapper
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import io.lumine.mythic.bukkit.utils.caffeine.cache.Caffeine
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.UUID

internal interface Display: Addon, State {

    val playerHolograms: Cache<UUID, List<HologramWrapper>>

    val tickDelay: Int
    val centreOffset: Double

    suspend fun startRenderLoop() {
        logger.info("Starting display render task")
        while (true) {
            server.onlinePlayers.forEach { player ->
                val plotId = playerService.getPlotId(player.uniqueId) ?: return@forEach
                render(player.uniqueId, plotId)

                delay(tickDelay.ticks)
            }
            Thread.sleep(3L)
        }
    }

    suspend fun render(playerId: UUID, plotId: UUID)

    suspend fun updateHolograms(playerId: UUID, plotId: UUID): List<HologramWrapper>

    suspend fun far(playerId: UUID, plotId: UUID, id: String)

    suspend fun close(playerId: UUID, plotId: UUID, id: String)
}