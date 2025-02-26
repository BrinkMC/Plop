package com.brinkmc.plop.plot.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.GuiUtils.description
import com.brinkmc.plop.shared.util.GuiUtils.name
import com.noxcrew.interfaces.view.InterfaceView
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class NexusManager(override val plugin: Plop): Addon, State {

    val NEXUS_BOOK = ItemStack(Material.WRITTEN_BOOK)
        .name(lang.decode(plotConfig.nexusConfig.bookName))
        .description("nexus.book-desc")

    val schematicName = plotConfig.nexusConfig.schematicName

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