package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.*
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.Locals.localWorld
import com.brinkmc.plop.shared.util.BukkitUtils.player
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds


class PlotClaimHandler(override val plugin: Plop): Addon, State {
    override suspend fun load() { }

    override suspend fun kill() { }

    fun updateBorder(player: UUID) { plugin.async {
        // Update player tracker for handy information
        playerTracker.refresh(player.player() ?: return@async)

        val bukkitPlayer = player.player() ?: run {
            logger.error("Failed to send fake border to player $player")
            return@async
        }

        val potentialPreview = plots.previewHandler.getPreview(player)
        if (potentialPreview != null) { // Check if player is in a preview
            plugin.syncScope {
                borderAPI.setBorder(bukkitPlayer, configHandler.plotConfig.getPlotMaxSize(potentialPreview.type).toDouble(), potentialPreview.viewPlot.value.toLocation())
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

    suspend fun performTeleportCountdown( // Takes seconds and extra action to perform on each tick
        player: Player,
        seconds: Int = 5,
        onTick: ((secondsLeft: Int) -> Unit)? = null
    ): MessageKey {
        val previousLoc = player.location.clone()

        for (i in 0 until seconds) {
            val secondsLeft = seconds - i
            val timeLeftPlaceholder = arrayOf(Placeholder.component("timeLeft", Component.text(secondsLeft)))

            messages.sendMiniMessage(player, MessageKey.TELEPORT_IN_PROGRESS, args = timeLeftPlaceholder)

            // Check if player moved
            if (player.location.x.roundToInt() != previousLoc.x.roundToInt() ||
                player.location.y.roundToInt() != previousLoc.y.roundToInt() ||
                player.location.z.roundToInt() != previousLoc.z.roundToInt()) {
                return MessageKey.TELEPORT_INTERRUPTED
            }

            messages.sendSound(player, SoundKey.CLICK)
            onTick?.invoke(secondsLeft)
            delay(1.seconds)
        }

        return MessageKey.TELEPORT_COMPLETE
    }

    suspend fun initiateClaim(player: UUID, plotType: PlotType) {
        val previewInstance = plots.previewHandler.getPreview(player)
        
        if (previewInstance == null) {
            player.player()?.sendMiniMessage(MessageKey.NO_PREVIEW)
            player.player()?.sendSound(SoundKey.FAILURE)
            return
        }

        val plotClaim = PlotClaim(
            previewInstance.viewPlot.value.toLocation(),
            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation(),
            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation()
        )

        val uuid = when (plotType) { // Determine plot ID based on plot type
            PlotType.PERSONAL -> {
                player
            }
            PlotType.GUILD -> {
                player.guild()?.id ?: return
            }
        }

        val newPlot = Plot(
            uuid,
            plotType,
            mutableListOf(),
            plotClaim,
            PlotVisit(level = 0, currentVisits = 0, historicalVisits = mutableListOf(), plotType = plotType),
            PlotSize(0, plotType),
            PlotFactory(0, mutableListOf(), plotType),
            PlotShop(0, plotType),
            PlotTotem(0, mutableListOf(),true, plotType) // No totems for a brand-new plot
        )

        plots.handler.addPlot(newPlot) // Register new plot in handler

        plots.previewHandler.claimPlot(player) // Finalise preview

        plugin.hooks.worldGuard.createRegion(uuid) // Create region in WorldGuard

        // Send in the schematic
        val schematic = plots.nexusManager.getSchematic()
        val location = newPlot.claim.home.clone().subtract(0.0, 1.0, 0.0) // Subtract 1 from Y to place the schematic on the ground

        plugin.syncScope {
            val editSession = WorldEdit.getInstance().newEditSession(location.world.localWorld())
            editSession.use {
                try {
                    val operation = ClipboardHolder(schematic)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.x, location.y, location.z))
                        .ignoreAirBlocks(false)
                        .build()
                    Operations.complete(operation)
                } catch (e: Exception) {
                    logger.error("Failed to paste schematic for plot $uuid")
                    logger.error(e.message)
                }
            }
        }
        // Teleport player to the new schematic
        player.player()?.teleportAsync(newPlot.claim.home)

        player.player()?.updateBorder() // Update the border again to new smaller size
    }
}