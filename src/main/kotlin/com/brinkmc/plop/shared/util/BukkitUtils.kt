package com.brinkmc.plop.shared.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

object BukkitUtils {
    fun UUID.player(): Player? {
        return Bukkit.getPlayer(this)
    }


    fun countItemsInInventory(playerId: UUID, itemToMatch: ItemStack?): Int? {
        return playerId.player()?.inventory?.contents?.filterNotNull()?.sumOf { invItem ->
            if (invItem.isSimilar(itemToMatch)) invItem.amount else 0
        }
    }

}