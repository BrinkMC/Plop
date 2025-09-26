package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.plot.constant.StringLocation
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.*
import com.brinkmc.plop.plot.dto.structure.Nexus
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.util.ClaimUtils
import com.brinkmc.plop.shared.util.fold
import com.brinkmc.plop.shared.util.withTimer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Location
import java.util.*
import kotlin.math.roundToInt


class PlotClaimService(override val plugin: Plop): Addon, State {

    override suspend fun load() { }

    override suspend fun kill() { }


    suspend fun performTeleportCountdown(playerId: UUID, seconds: Int = 5, location: Location): Boolean {
        val player = playerService.getPlayer(playerId) ?: return false
        val previousLoc = player.location.clone()

        withTimer(seconds) { secondsLeft ->
            val timeLeftPlaceholder = arrayOf(Placeholder.component("timeLeft", Component.text(secondsLeft)))
            messages.sendMiniMessage(player, MessageKey.TELEPORT_IN_PROGRESS, args = timeLeftPlaceholder)

            if (player.location.x.roundToInt() != previousLoc.x.roundToInt() ||
                player.location.y.roundToInt() != previousLoc.y.roundToInt() ||
                player.location.z.roundToInt() != previousLoc.z.roundToInt()) {
                return@withTimer true
            }

            messages.sendSound(player, SoundKey.CLICK)
            return@withTimer false
        }.fold(
            onSuccess = {
                messages.sendSound(player, SoundKey.TELEPORT)
                messages.sendMiniMessage(player, MessageKey.TELEPORT_COMPLETE)
                player.teleportAsync(location)
                return true
            },
            onInterrupted = {
                messages.sendSound(player, SoundKey.FAILURE)
                messages.sendMiniMessage(player, MessageKey.TELEPORT_INTERRUPTED)
                return false
            }
        )
    }

    suspend fun createPlot(playerId: UUID, plotType: PlotType, stringLocation: StringLocation) {
        val location = stringLocation.toLocation()

        val plotClaim = PlotClaim(
            location.clone(),
            ClaimUtils.getSafe(location.clone()),
            ClaimUtils.getSafe(location.clone())
        )

        val plotId = when (plotType) { // Determine plot ID based on plot type
            PlotType.PERSONAL -> {
                playerId
            }
            PlotType.GUILD -> {
                hookService.guilds.getGuildFromPlayer(playerId)?.id ?: run {
                    messages.sendMiniMessage(player, MessageKey.NO_GUILD)
                    messages.sendSound(player, SoundKey.FAILURE)
                    return
                }
            }
        }

        val newPlot = Plot(
            plotId,
            plotType,
            plotClaim,
            PlotVisit(true, 0, 0, mutableListOf()),
            PlotSize(0),
            PlotFactory(0),
            PlotShop(0),
            PlotTotem(0, mutableListOf(), true),
            PlotFueler(0, null),
            PlotNexus(mutableListOf<Nexus>()),
        )

        plotService.addPlot(newPlot) // Register new plot in handler
        performTeleportCountdown(playerId, 5, newPlot.claim.home) // Teleport player to the new schematic

        // Hooks
        hookService.worldGuard.createRegion(plotId)
        plotNexusService.placeNexus(plotId)

        // Teleport player to the new schematic
        player.teleportAsync(newPlot.claim.home)
        messages.sendMiniMessage(player, MessageKey.PLOT_CREATED)
        messages.sendSound(player, SoundKey.SUCCESS)

        plotBorderService.updateBorder(playerId)

    }

    suspend fun addMemberToRegion(plotId: UUID, memberId: UUID) {
        plugin.asyncScope {
            hookService.worldGuard.addMember(plotId, memberId)
        }
    }

    suspend fun removeMemberFromRegion(plotId: UUID, memberId: UUID) {
        plugin.asyncScope {
            hookService.worldGuard.removeMember(plotId, memberId)
        }
    }

    suspend fun createPlotRegion(plotId: UUID) {
        plugin.asyncScope {
            hookService.worldGuard.createRegion(plotId)
        }
    }


    // Handle plot claim

    private suspend fun getPlotClaim(plotId: UUID) = plotService.getPlotClaim(plotId)

    private suspend fun getPlotCentre(plotId: UUID): Location? {
        return getPlotClaim(plotId)?.centre
    }

    private suspend fun getPlotHome(plotId: UUID): Location? {
        return getPlotClaim(plotId)?.home
    }

    private suspend fun getPlotVisit(plotId: UUID): Location? {
        return getPlotClaim(plotId)?.visit
    }

    private suspend fun setPlotHome(plotId: UUID, location: Location) {
        getPlotClaim(plotId)?.setHome(location)
    }

    private suspend fun setPlotVisit(plotId: UUID, location: Location) {
        getPlotClaim(plotId)?.setVisit(location)
    }











//    suspend fun initiateClaim(player: UUID, plotType: PlotType) {
//        val previewInstance = plots.previewHandler.getPreview(player)
//
//        if (previewInstance == null) {
//            player.player()?.sendMiniMessage(MessageKey.NO_PREVIEW)
//            player.player()?.sendSound(SoundKey.FAILURE)
//            return
//        }
//
//        val plotClaim = PlotClaim(
//            previewInstance.viewPlot.value.toLocation(),
//            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation(),
//            previewInstance.viewPlot.value.toLocation().getSafeDestination() ?: previewInstance.viewPlot.value.toLocation()
//        )
//
//        val uuid = when (plotType) { // Determine plot ID based on plot type
//            PlotType.PERSONAL -> {
//                player
//            }
//            PlotType.GUILD -> {
//                player.guild()?.id ?: return
//            }
//        }
//
//        val newPlot = Plot(
//            uuid,
//            plotType,
//            mutableListOf(),
//            plotClaim,
//            PlotVisit(level = 0, currentVisits = 0, historicalVisits = mutableListOf(), plotType = plotType),
//            PlotSize(0, plotType),
//            PlotFactory(0, mutableListOf(), plotType),
//            PlotShop(0, plotType),
//            PlotTotem(0, mutableListOf(),true, plotType) // No totems for a brand-new plot
//        )
//
//        plots.handler.addPlot(newPlot) // Register new plot in handler
//
//        plots.previewHandler.claimPlot(player) // Finalise preview
//
//        plugin.hooks.worldGuard.createRegion(uuid) // Create region in WorldGuard
//
//        // Send in the schematic
//        val schematic = plots.nexusService.getSchematic()
//        val location = newPlot.claim.home.clone().subtract(0.0, 1.0, 0.0) // Subtract 1 from Y to place the schematic on the ground
//
//        plugin.syncScope {
//            val editSession = WorldEdit.getInstance().newEditSession(location.world.localWorld())
//            editSession.use {
//                try {
//                    val operation = ClipboardHolder(schematic)
//                        .createPaste(editSession)
//                        .to(BlockVector3.at(location.x, location.y, location.z))
//                        .ignoreAirBlocks(false)
//                        .build()
//                    Operations.complete(operation)
//                } catch (e: Exception) {
//                    logger.error("Failed to paste schematic for plot $uuid")
//                    logger.error(e.message)
//                }
//            }
//        }
//        // Teleport player to the new schematic
//        player.player()?.teleportAsync(newPlot.claim.home)
//
//        player.player()?.updateBorder() // Update the border again to new smaller size
//    }
}