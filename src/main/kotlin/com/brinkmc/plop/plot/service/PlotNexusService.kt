package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotItems
import com.brinkmc.plop.plot.dto.modifier.PlotNexus
import com.brinkmc.plop.plot.dto.structure.Nexus
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.PermissionKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.util.LocationString.fullString
import com.brinkmc.plop.shared.util.LocationString.toLocation
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Lectern
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.UUID

class PlotNexusService(override val plugin: Plop): Addon, State {

    private suspend fun getNexusBook(): ItemStack {
        return itemService.createTrackedItem(PlotItems.NEXUS_BOOK)
    }

    private fun isNexusBook(item: ItemStack): Boolean {
        if (item.type != Material.WRITTEN_BOOK) return false
        return itemService.getTrackingType(item) == PlotItems.NEXUS_BOOK
    }

    private fun isLectern(location: Location): Boolean {
        return location.block.type == Material.LECTERN
    }

    // Getters

    private suspend fun getPlotNexus(plotId: UUID): PlotNexus? = plotService.getPlotNexus(plotId)

    suspend fun getNexi(plotId: UUID): List<Location?> {
        val plotNexus = getPlotNexus(plotId) ?: return emptyList()

        return plotNexus.getNexus().map { it.location.toLocation() }
    }

    // Setters

    private suspend fun addNexusToPlot(plotId: UUID, location: Location) {
        val plotNexus = getPlotNexus(plotId) ?: return

        val nexus = Nexus(
            location = location.fullString()
        )

        plotNexus.addNexus(nexus)
    }

    private suspend fun removeNexusFromPlot(plotId: UUID, location: Location) {
        val plotNexus = getPlotNexus(plotId) ?: return

        plotNexus.removeNexus(location.block.location.fullString())
    }

    private suspend fun removeBookFromLectern(location: Location) {
        if (!isLectern(location)) return

        val lectern = location.block.state as Lectern
        lectern.inventory.setItem(0, null)
    }

    // Public

    suspend fun giveNexusBook(playerId: UUID, location: Location): ServiceResult {
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ServiceResult.Failure(MessageKey.NO_PLOT, SoundKey.FAILURE)

        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE) // Definitely the owner
        }

        if (playerService.hasItem(playerId, getNexusBook())) {
            return ServiceResult.Failure(MessageKey.ALREADY_OWN_NEXUS_BOOK, SoundKey.FAILURE)
        }

        if (!playerService.giveItem(playerId, getNexusBook())) {
            return ServiceResult.Failure(MessageKey.INVENTORY_FULL, SoundKey.FAILURE)
        }

        return ServiceResult.Success(MessageKey.GIVE_NEXUS_BOOK, SoundKey.SUCCESS)
    }

    suspend fun antiNexusBook(playerId: UUID, location: Location) {
        plotService.getPlotIdFromLocation(location) ?: run {
            while (playerService.hasItem(playerId, getNexusBook())) {
                playerService.removeItem(playerId, getNexusBook())
            }
            return
        }
    }

    suspend fun nexusInteraction(playerId: UUID, location: Location, action: Action): ServiceResult {
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ServiceResult.Failure()

        // Try to check if location block is lectern
        if (!isLectern(location)) {
            return ServiceResult.Failure()
        }

        val lectern = location.block.state as Lectern

        val item = lectern.inventory.getItem(0)

        if (item == null || !isNexusBook(item)) {
            return ServiceResult.Failure()
        }

        val plotNexus = getPlotNexus(plotId) ?: return ServiceResult.Failure()

        if (!plotService.isPlotMember(plotId, playerId)) {
            return ServiceResult.Failure(MessageKey.NOT_OWNER, SoundKey.FAILURE) // Definitely the owner
        }

        if (playerService.hasPermission(playerId, PermissionKey.USE_NEXUS)) {
            return ServiceResult.Failure(MessageKey.NO_PERMISSION, SoundKey.FAILURE) // Definitely the owner
        }

        // Determined allowed to use, now handle action

        when (action) {
            Action.RIGHT_CLICK_BLOCK if playerService.isSneaking(playerId) -> {
                // Remove nexus
                removeBookFromLectern(lectern.block.location)
                removeNexusFromPlot(plotId, lectern.block.location)
                playerService.giveItem(playerId, getNexusBook()) // Give book to player

            }
            Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_BLOCK -> {
                // Open nexus menu
                menuService.nexusMenu.open(playerId, plotId)
            }
            else -> {
                return ServiceResult.Failure(MessageKey.UNRECOGNISED_ACTION, SoundKey.FAILURE)
            }
        }

        return ServiceResult.Success(MessageKey.OPENING_NEXUS, SoundKey.SUCCESS)
    }

    suspend fun createNexus(location: Location, item: ItemStack): ServiceResult {
        val plotId = plotService.getPlotIdFromLocation(location) ?: return ServiceResult.Failure()

        // Try to check if location block is lectern
        if (!isLectern(location)) {
            return ServiceResult.Failure()
        }

        val lectern = location.block.state as Lectern

        // Check if item is a nexus book
        if (!isNexusBook(item)) {
            return ServiceResult.Failure()
        }

        addNexusToPlot(plotId, lectern.block.location)


        return ServiceResult.Success(MessageKey.NEXUS_CREATED, SoundKey.SUCCESS)
    }

    suspend fun deleteNexus(location: Location) {
        val plotId = plotService.getPlotIdFromLocation(location) ?: return

        // Try to check if location block is lectern
        if (!isLectern(location)) {
            return
        }

        val lectern = location.block.state as Lectern

        val item = lectern.inventory.getItem(0)

        if (item == null || !isNexusBook(item)) {
            return
        }

        removeNexusFromPlot(plotId, lectern.block.location)
    }

    val schematicName = configService.plotConfig.nexusConfig.schematicName

    fun getSchematic(): Clipboard? {
        val worldEditDir = server.pluginManager.getPlugin("WorldEdit")!!.dataFolder
        val schematicFile = File(worldEditDir, "schematics/$schematicName.schem")

        val format = ClipboardFormats.findByFile(schematicFile)


        val clipboard = try {
            format?.getReader(FileInputStream(schematicFile)).use { reader ->
                reader?.read()
            }
        } catch (e: IOException) {
            logger.error(e.message)
            null
        }

        return clipboard
    }

    override suspend fun load() {

    }

    override suspend fun kill() {

    }
}