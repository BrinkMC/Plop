package com.brinkmc.plop.shared.hook.api

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.items.MythicItem
import org.bukkit.inventory.ItemStack

class MythicMobs(override val plugin: Plop): Addon, State {

    private lateinit var mythicAPI:  MythicBukkit

    override suspend fun load() {
        mythicAPI = MythicBukkit.inst()
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    fun isMythicItem(itemStack: ItemStack): Boolean {
        return mythicAPI.itemManager.isMythicItem(itemStack)
    }

    fun getItem(itemStack: ItemStack): MythicItem? {
        if (!isMythicItem(itemStack)) {
            return null
        }
        val mythicType = getMythicType(itemStack)
        return mythicAPI.itemManager.getItem(mythicType).get()
    }

    private fun getMythicType(itemStack: ItemStack): String? {
        return mythicAPI.itemManager.getMythicTypeFromItem(itemStack)
    }

}