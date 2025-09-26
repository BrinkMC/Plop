package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.PermissionKey
import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PlayerService(override val plugin: Plop): Addon, State {

    // Location tracking stuff

    private val locations = Caffeine.newBuilder().asLoadingCache<Player, UUID?> {
        plotService.getPlotIdFromLocation(it.location)
    }

    suspend fun getPlotId(playerId: UUID): UUID? {
        val player = getPlayer(playerId) ?: return null
        return locations.get(player)
    }

    fun clearCache(player: Player) {
        locations.invalidate(player)
    }


    // General player activity

    private fun getPlayer(playerId: UUID): Player? {
        return Bukkit.getPlayer(playerId)
    }

    fun getLocation(playerId: UUID): org.bukkit.Location? {
        val player = getPlayer(playerId) ?: return null
        return player.location
    }

    fun hasPermission(playerId: UUID, permission: PermissionKey): Boolean {
        val player = getPlayer(playerId) ?: return false
        return player.hasPermission(permission.permission)
    }

    fun giveItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getPlayer(playerId) ?: return false
        return player.inventory.addItem(item).isEmpty() // If is empty, then was success, therefore true
    }

    fun removeItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getPlayer(playerId) ?: return false
        return player.inventory.removeItem(item).isEmpty() // If is empty, then was success, therefore true
    }

    fun hasItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getPlayer(playerId) ?: return false
        return player.inventory.containsAtLeast(item, item.amount)
    }

    fun isSneaking(playerId: UUID): Boolean {
        val player = getPlayer(playerId) ?: return false
        return player.isSneaking
    }

    fun isOnline(playerId: UUID): Boolean {
        return getPlayer(playerId) != null
    }

    fun getDisplayName(playerId: UUID): Component? {
        val player = getPlayer(playerId) ?: return null
        return player.displayName()
    }

    fun allowFlight(playerId: UUID, allow: Boolean) {
        val player = getPlayer(playerId) ?: return
        player.allowFlight = allow
        player.isFlying = allow
    }

    fun setInventory(playerId: UUID, items: Array<ItemStack?>) {
        val player = getPlayer(playerId) ?: return
        player.inventory.contents = items
    }

    fun getInventory(playerId: UUID): Array<ItemStack?>? {
        val player = getPlayer(playerId) ?: return null
        return player.inventory.contents
    }

    fun teleport(playerId: UUID, location: org.bukkit.Location, reason: PlayerTeleportEvent.TeleportCause) {
        val player = getPlayer(playerId) ?: return
        player.teleportAsync(location, reason)
    }

    // Actions

    fun sendMessage(playerId: UUID, message: Component) {
        val player = getPlayer(playerId) ?: return
        player.sendMessage(message)
    }

    fun showTitle(playerId: UUID, title: Title) {
        val player = getPlayer(playerId) ?: return
        player.showTitle(title)
    }

    fun sendActionBar(playerId: UUID, actionBar: Component) {
        val player = getPlayer(playerId) ?: return
        player.sendActionBar(actionBar)
    }

    fun playSound(playerId: UUID, sound: Sound) {
        val player = getPlayer(playerId) ?: return
        player.playSound(sound)
    }


    override suspend fun load() { }

    override suspend fun kill() {
        locations.invalidateAll()
    }


}