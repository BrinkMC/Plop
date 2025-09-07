package com.brinkmc.plop.shop.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class ShopStockService(override val plugin: Plop): Addon, State {

    val playerInventorySnapshot = HashMap<UUID, Array<ItemStack?>>()

    override suspend fun load() {
        TODO("Not yet implemented")
    }

    override suspend fun kill() {
        TODO("Not yet implemented")
    }

    private fun snapshotInventory(player: Player) {
        playerInventorySnapshot[player.uniqueId] = player.inventory.contents.clone()
    }

    private fun getInventory(player: Player): Array<ItemStack?>? {
        return playerInventorySnapshot[player.uniqueId]
    }

    private fun retrieveInventory(player: Player): Array<ItemStack?>? {
        val snapshot = getInventory(player)
        playerInventorySnapshot.remove(player.uniqueId)
        return snapshot
    }

    fun storeInventory(player: Player): Array<ItemStack?> {
        snapshotInventory(player)
        return getInventory(player)!!
    }

    fun returnInventory(player: Player) {
        val snapshot = retrieveInventory(player)
        player.inventory.contents = snapshot
    }

}