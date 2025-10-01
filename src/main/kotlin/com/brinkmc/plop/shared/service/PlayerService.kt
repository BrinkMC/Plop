package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.PermissionKey
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.brinkmc.plop.shared.util.fold
import com.brinkmc.plop.shared.util.withTimer
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.yannicklamprecht.worldborder.api.WorldBorderApi
import com.sksamuel.aedile.core.asLoadingCache
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.math.roundToInt

class PlayerService(override val plugin: Plop): Addon, State {

    // Location tracking stuff
    private lateinit var borderAPI: WorldBorderApi

    private val locations = Caffeine.newBuilder().asLoadingCache<Player, UUID?> {
        plotService.getPlotIdFromLocation(it.location)
    }

    suspend fun getPlotId(playerId: UUID): UUID? {
        val player = getOfflinePlayer(playerId).player ?: return null
        return locations.get(player)
    }

    fun clearCache(player: Player) {
        locations.invalidate(player)
    }

    // General player activity

    private fun getOfflinePlayer(playerId: UUID): OfflinePlayer {
        return Bukkit.getOfflinePlayer(playerId)
    }

    fun getLocation(playerId: UUID): org.bukkit.Location? {
        val player = getOfflinePlayer(playerId)
        return player.location
    }

    fun hasPermission(playerId: UUID, permission: PermissionKey): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.hasPermission(permission.permission)
    }

    fun giveItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.inventory.addItem(item).isEmpty() // If is empty, then was success, therefore true
    }

    fun removeItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.inventory.removeItem(item).isEmpty() // If is empty, then was success, therefore true
    }

    fun hasItem(playerId: UUID, item: ItemStack): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.inventory.containsAtLeast(item, item.amount)
    }

    fun isSneaking(playerId: UUID): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.isSneaking
    }

    fun isOnline(playerId: UUID): Boolean {
        return getOfflinePlayer(playerId).player != null
    }

    fun getDisplayName(playerId: UUID): Component? {
        val player = getOfflinePlayer(playerId).player ?: return null
        return player.displayName()
    }

    fun allowFlight(playerId: UUID, allow: Boolean) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.allowFlight = allow
        player.isFlying = allow
    }

    fun teleport(playerId: UUID, location: org.bukkit.Location, reason: PlayerTeleportEvent.TeleportCause) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.teleportAsync(location, reason)
        plotBorderService.updateBorder(playerId)
    }

    fun getLivingEntity(playerId: UUID): LivingEntity {
        val player = getOfflinePlayer(playerId)
        return player.player ?: player as LivingEntity
    }

    fun setBorder(playerId: UUID, plotType: PlotType, centre: Location, isPreview: Boolean, plotSize: Int? = null) {
        plugin.sync {
            val size = when(isPreview) {
                true -> {
                    configService.plotConfig.getPlotMaxSize(plotType).toDouble()
                }
                false -> {
                    plotSize?.toDouble()
                }
            } ?: return@sync

            borderAPI.setBorder(
                getOfflinePlayer(playerId).player,
                size,
                centre
            )
        }
    }

    fun resetBorder(playerId: UUID) {
        plugin.sync {
            borderAPI.resetWorldBorderToGlobal(getOfflinePlayer(playerId).player)
        }
    }

    // Inventory

    fun setInventory(playerId: UUID, items: Array<ItemStack?>) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.inventory.contents = items
    }

    fun getInventory(playerId: UUID): Array<ItemStack?>? {
        val player = getOfflinePlayer(playerId).player ?: return null
        return player.inventory.contents
    }

    fun addToInventory(playerId: UUID, item: ItemStack): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.inventory.addItem(item).isEmpty() // If is empty, then was success, therefore true
    }

    fun removeItemFromInventory(playerId: UUID, item: ItemStack): Boolean {
        val player = getOfflinePlayer(playerId).player ?: return false
        return player.inventory.removeItemAnySlot(item).isEmpty() // If is empty, then was success, therefore true
    }

    // Actions

    fun sendMessage(playerId: UUID, message: Component) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.sendMessage(message)
    }

    fun showTitle(playerId: UUID, title: Title) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.showTitle(title)
    }

    fun sendActionBar(playerId: UUID, actionBar: Component) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.sendActionBar(actionBar)
    }

    fun playSound(playerId: UUID, sound: Sound) {
        val player = getOfflinePlayer(playerId).player ?: return
        player.playSound(sound)
    }

    // custom actions

    suspend fun performTeleportCountdown(playerId: UUID, seconds: Int = 5, location: Location): Boolean {
        val previousLoc = playerService.getLocation(playerId)?.clone() ?: return false

        withTimer(seconds) { secondsLeft ->
            val timeLeftPlaceholder = arrayOf(Placeholder.component("timeLeft", Component.text(secondsLeft)))

            messages.sendMiniMessage(playerId, MessageKey.TELEPORT_IN_PROGRESS, args = timeLeftPlaceholder)
            val currentLocation = playerService.getLocation(playerId) ?: return@withTimer true
            if (currentLocation.x.roundToInt() != previousLoc.x.roundToInt() ||
                currentLocation.y.roundToInt() != previousLoc.y.roundToInt() ||
                currentLocation.z.roundToInt() != previousLoc.z.roundToInt()
            ) {
                return@withTimer true
            }

            messages.sendSound(playerId, SoundKey.CLICK)
            return@withTimer false
        }.fold(
            onSuccess = {
                messages.sendSound(playerId, SoundKey.TELEPORT)
                messages.sendMiniMessage(playerId, MessageKey.TELEPORT_COMPLETE)
                teleport(playerId, location, PlayerTeleportEvent.TeleportCause.PLUGIN)
                return true
            },
            onInterrupted = {
                messages.sendSound(playerId, SoundKey.FAILURE)
                messages.sendMiniMessage(playerId, MessageKey.TELEPORT_INTERRUPTED)
                return false
            }
        )
    }


    override suspend fun load() {
        borderAPI = server.servicesManager.getRegistration<WorldBorderApi?>(WorldBorderApi::class.java)?.provider ?: run {
            logger.error("Failed to get WorldBorderAPI")
            return
        }
    }

    override suspend fun kill() {
        locations.invalidateAll()
    }


}