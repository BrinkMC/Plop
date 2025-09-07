package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.design.enums.MessageKey
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class PlotNexusService(override val plugin: Plop): Addon, State {

    val NEXUS_BOOK = ItemStack(Material.WRITTEN_BOOK)
        .name(MessageKey.NEXUS_BOOK_NAME)
        .description(MessageKey.NEXUS_BOOK_DESC)

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