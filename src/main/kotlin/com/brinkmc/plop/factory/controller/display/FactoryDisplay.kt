package com.brinkmc.plop.factory.controller.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Display
import com.brinkmc.plop.shared.dto.HologramWrapper
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.TextHologramData
import org.bukkit.Location
import org.joml.Vector3f
import java.util.UUID
import kotlin.text.compareTo

class FactoryDisplay(override val plugin: Plop): Display {

    override val playerHolograms: Cache<UUID, List<HologramWrapper>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .asCache()

    override val tickDelay: Int = 3
    override val centreOffset: Double = 0.5

    override suspend fun render(playerId: UUID, plotId: UUID) {
        val playerLocation = playerService.getLocation(playerId) ?: return
        val closestFactory: Pair<UUID, Double>? =
            factoryService.getFactories(plotId)
                .mapNotNull { factoryId ->
                    val factoryLocation = factoryService.getFactoryLocation(factoryId)?.toLocation() ?: return@mapNotNull null
                    val distance = factoryLocation.add(centreOffset, 0.0, centreOffset).distanceSquared(playerLocation)
                    if (distance < configService.factoryConfig.viewDistance) {
                        factoryId to distance
                    } else null
                }
                .minByOrNull { it.second }

        val previous = playerHolograms.getIfPresent(playerId)

        when (closestFactory) {
            null if !previous.isNullOrEmpty() -> {
                far(playerId, plotId, previous.first().id)
            }
            null -> {
                // No factory nearby and no previous hologram, do nothing
            }
            else -> {
                val (factoryId, _) = closestFactory
                close(playerId, plotId, factoryId.toString())
            }
        }
    }

    override suspend fun far(playerId: UUID, plotId: UUID, id: String) {
        playerHolograms[playerId] = emptyList()
        logger.info("Removed factory hologram for player $playerId")

        val holograms = updateHolograms(playerId, plotId)
    }

    override suspend fun close(playerId: UUID, plotId: UUID, id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateHolograms(playerId: UUID, plotId: UUID, id: String): List<HologramWrapper> {
        val components = textHologram(configService.factoryConfig.display, playerId, plotId, UUID.fromString(id))

        val hologram = TextHologramData("factory-$id", )
        val wrappers = components.map {
            HologramWrapper(id,
                )
        }
    }

    suspend fun textHologram(lines: List<String>, playerId: UUID, plotId: UUID? = null, factoryId: UUID): HologramWrapper? {
        val offset = lines.last().substringBefore(":").toDoubleOrNull() ?: 0.0
        val scaleNumber = lines.last().substringAfter(":").toFloatOrNull() ?: 1.0f
        val scale = Vector3f(scaleNumber, scaleNumber, scaleNumber)

        val hologramLocation = factoryService.getFactoryLocation(factoryId)?.toLocation()
        hologramLocation?.add(centreOffset, offset, centreOffset) ?: return null

        val hologram = TextHologramData(
            "factory-$factoryId",
            hologramLocation
        )
        hologram.text = lines.dropLast(1).map { line ->
            messages.resolveTags(line, playerId, plotId, factoryId = factoryId)
        }

        val wrapper = HologramWrapper(
            id = factoryId.toString(),
            hologram = hologramService.createHologram(hologram),
            offset = offset,
            scale = scale
        )

    }


    override suspend fun load() {
        startRenderLoop()
    }

    override suspend fun kill() {

    }

}