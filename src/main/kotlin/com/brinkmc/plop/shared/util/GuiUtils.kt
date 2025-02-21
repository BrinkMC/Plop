package com.brinkmc.plop.shared.util

import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildMember
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.stream.Collectors


/*
Credit to NoxCrew in the interfaces-kotlin library for these handy extensions
 */

object GuiUtils {
    fun ItemStack.name(name: String): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(Component.text(name))
        }
        return this
    }

    fun ItemStack.name(name: Component): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(name)
        }
        return this
    }

    fun ItemStack.description(description: String): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(Component.text(description)))
        }
        return this
    }

    fun ItemStack.description(description: Component): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(description))
        }
        return this
    }

    fun ItemStack.setSkull(owner: OfflinePlayer?): ItemStack {
        val meta = itemMeta as org.bukkit.inventory.meta.SkullMeta
        meta.playerProfile = owner?.playerProfile
        itemMeta = meta
        return this
    }

    fun ItemStack.setSkull(owner: Guild?): ItemStack {
        val meta = itemMeta as org.bukkit.inventory.meta.SkullMeta
        meta.playerProfile = (owner?.guildSkull?.itemStack?.itemMeta as SkullMeta).playerProfile
        itemMeta = meta
        return this
    }
}