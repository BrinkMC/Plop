package com.brinkmc.plop.factory.controller.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Display
import com.brinkmc.plop.shared.dto.HologramWrapper
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.Cache
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.TextHologramData
import kotlinx.coroutines.Job
import org.bukkit.Location
import org.joml.Vector3f
import java.util.UUID
import kotlin.text.compareTo

class FactoryDisplay(override val plugin: Plop) : Display {

    override val playerHolograms: Cache<UUID, List<HologramWrapper>> = Caffeine.newBuilder()
        .maximumSize(1000)
        .asCache()

    override val tickDelay: Int = 3
    override val centreOffset: Double = 0.5

    override var rendering: Boolean = true
    override var render: Job? = null

    override suspend fun findClosestMarker(playerId: UUID, plotId: UUID): Pair<Any, Double>? {
        val playerLocation = playerService.getLocation(playerId) ?: return null
        // getFactories and getFactoryLocation may be suspend, so this must be suspend if needed
        // If they are suspend, move this logic into buildHolograms and just return a marker id here
        val factories = factoryService.getFactories(plotId)

        return factories.mapNotNull { factoryId ->
            val factoryLocation = factoryService.getFactoryLocation(factoryId)?.toLocation() ?: return@mapNotNull null
            val distance = factoryLocation.add(centreOffset, 0.0, centreOffset).distanceSquared(playerLocation)
            if (distance < configService.factoryConfig.viewDistance) {

                factoryId to distance
            } else null
        }.minByOrNull { it.second }
    }

    override suspend fun getViewDistance(): Double = configService.factoryConfig.viewDistance.toDouble()

    override suspend fun buildHolograms(playerId: UUID, plotId: UUID, marker: Any): List<HologramWrapper> {
        val factoryId = marker as? UUID ?: return emptyList()

        val lines = configService.factoryConfig.display
        val offset = lines.lastOrNull()?.substringBefore(":")?.toDoubleOrNull() ?: 0.0
        val scaleNumber = lines.lastOrNull()?.substringAfter(":")?.toFloatOrNull() ?: 1.0f
        val scale = Vector3f(scaleNumber, scaleNumber, scaleNumber)

        val location = factoryService.getFactoryLocation(factoryId)?.toLocation()?.apply { add(centreOffset, offset, centreOffset) }
            ?: return emptyList()

        val text = lines.dropLast(1).map { messages.resolveTags(it, playerId, plotId, factoryId = factoryId) }
        val data = TextHologramData("factory-$factoryId", location).apply { this.text = text }

        val hologram = hologramService.createTextHologram(data)
        return listOf(HologramWrapper(factoryId.toString(), hologram, offset, scale))
    }

    override suspend fun load() {
        startRenderLoop()
    }

    override suspend fun kill() {
        stopRenderLoop()
    }
}
//    override suspend fun render(playerId: UUID, plotId: UUID) {
//        val playerLocation = playerService.getLocation(playerId) ?: return
//        val closestFactory: Pair<UUID, Double>? =
//            factoryService.getFactories(plotId)
//                .mapNotNull { factoryId ->
//                    val factoryLocation = factoryService.getFactoryLocation(factoryId)?.toLocation() ?: return@mapNotNull null
//                    val distance = factoryLocation.add(centreOffset, 0.0, centreOffset).distanceSquared(playerLocation)
//                    if (distance < configService.factoryConfig.viewDistance) {
//                        factoryId to distance
//                    } else null
//                }
//                .minByOrNull { it.second }
//
//        val previous = playerHolograms.getIfPresent(playerId)
//
//        when (closestFactory) {
//            null if !previous.isNullOrEmpty() -> {
//                far(playerId, plotId, previous.first().id)
//            }
//            null -> {
//                // No factory nearby and no previous hologram, do nothing
//            }
//            else -> {
//                val (factoryId, _) = closestFactory
//                close(playerId, plotId, factoryId.toString())
//            }
//        }
//    }
//
//    override suspend fun far(playerId: UUID, plotId: UUID, id: String) {
//        playerHolograms[playerId] = emptyList()
//        logger.info("Removed factory hologram for player $playerId")
//
//        val holograms = updateHolograms(playerId, plotId)
//    }
//
//    override suspend fun close(playerId: UUID, plotId: UUID, id: String) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun updateHolograms(playerId: UUID, plotId: UUID, id: String): List<HologramWrapper> {
//
//        // Get previous hologram if exists and the SAME factory, otherwise create a new one
//        val previousHologram: List<HologramWrapper> = playerHolograms.getIfPresent(playerId)?.filter { it.id == id } ?:
//        listOfNotNull(
//            textHologram(configService.factoryConfig.display, playerId, plotId, UUID.fromString(id))
//        )
//
//        for (hologram in previousHologram) {
//            val textData = hologram.hologram.data as TextHologramData
//            textData.text = configService.factoryConfig.display.dropLast(1).map { line ->
//                messages.resolveTags(line, playerId, plotId, factoryId = UUID.fromString(id))
//            }
//        }
//    }
//
//    suspend fun textHologram(lines: List<String>, playerId: UUID, plotId: UUID? = null, factoryId: UUID): HologramWrapper? {
//        val offset = lines.last().substringBefore(":").toDoubleOrNull() ?: 0.0
//        val scaleNumber = lines.last().substringAfter(":").toFloatOrNull() ?: 1.0f
//        val scale = Vector3f(scaleNumber, scaleNumber, scaleNumber)
//
//        val hologramLocation = factoryService.getFactoryLocation(factoryId)?.toLocation()
//        hologramLocation?.add(centreOffset, offset, centreOffset) ?: return null
//
//        val hologram = TextHologramData(
//            "factory-$factoryId",
//            hologramLocation
//        )
//        hologram.text = lines.dropLast(1).map { line ->
//            messages.resolveTags(line, playerId, plotId, factoryId = factoryId)
//        }
//
//        val wrapper = HologramWrapper(
//            id = factoryId.toString(),
//            hologram = hologramService.createTextHologram(hologram),
//            offset = offset,
//            scale = scale
//        )
//        return wrapper
//    }
//
//    override suspend fun load() {
//        startRenderLoop()
//    }
//
//    override suspend fun kill() {
//
//    }
