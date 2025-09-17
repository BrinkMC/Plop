package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.BukkitUtils
import com.brinkmc.plop.shared.util.BukkitUtils.player
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import org.bukkit.Bukkit
import java.util.UUID

class PlotBorderService(override val plugin: Plop): State, Addon {

    private lateinit var borderAPI: WorldBorderApi

    override suspend fun load() {
        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return
        }
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun updateBorder(playerId: UUID) { plugin.async {
        // Update player tracker for handy information
        val player = Bukkit.getPlayer(playerId) ?: run {
            logger.error("Failed to send fake border to player $playerId")
            return@async
        }

        val plotId = playerService.getPlotId(player)
        val previewInstance = plotPreviewService.getPreview(playerId)

        val potentialPreview = plotPreviewService.getPreview(player)
        if (potentialPreview != null) { // Check if player is in a preview
            plugin.syncScope {
                borderAPI.setBorder(bukkitPlayer, configService.plotConfig.getPlotMaxSize(potentialPreview.type).toDouble(), potentialPreview.viewPlot.value.toLocation())
            }
            return@async
        }


        if (bukkitPlayer.world.isPlotWorld()) { // Run logic for getting plot they are in
            val plot = getPlotFromLocation(bukkitPlayer.location) ?:  return@async // Make sure plot isn't null

            syncScope {
                borderAPI.setBorder(bukkitPlayer, plot.size.current.toDouble(), plot.claim.centre) // Send the player the border
            }
        } else {
            syncScope {
                borderAPI.resetWorldBorderToGlobal(bukkitPlayer)
            }
        }
    } }
}