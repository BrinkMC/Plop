package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.modifier.*
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Locals.localWorld
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import java.util.*


class PlotClaimHandler(override val plugin: Plop): Addon, State {
    override suspend fun load() { }

    override suspend fun kill() { }

    suspend fun initiateClaim(player: UUID, plotType: PlotType) {
        val previewInstance = plots.previewHandler.getPreview(player)
        
        if (previewInstance == null) {
            player.player()?.sendMiniMessage("preview.start.no-preview")
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
            PlotVisit(true, 0, 0, mutableListOf(), plotType),
            PlotSize(0, plotType),
            PlotFactory(0, mutableListOf(), plotType),
            PlotShop(0, mutableListOf(), plotType),
            PlotTotem(0, mutableListOf(),true, plotType) // No totems for a brand-new plot
        )

        plots.handler.addPlot(newPlot) // Register new plot in handler

        plots.previewHandler.claimPlot(player) // Finalise preview

        plugin.hooks.worldGuard.createRegion(uuid) // Create region in WorldGuard

        // Send in the schematic
        val schematic = plots.nexusManager.getSchematic()
        val location = newPlot.claim.home.subtract(0.0, 1.0, 0.0) // Subtract 1 from Y to place the schematic on the ground

        syncScope {
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

            // Teleport player to the new schematic
            player.player()?.teleport(location.add(0.0, 0.1, 0.0))
        }
        player.player()?.updateBorder() // Update the border again to new smaller size
    }
}