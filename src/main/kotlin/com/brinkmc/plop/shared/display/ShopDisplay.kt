package com.brinkmc.plop.shared.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Display
import com.brinkmc.plop.shop.shop.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.ItemHologramData
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopDisplay(override val plugin: Plop): Addon, State {

    val active = Caffeine.newBuilder().asCache<Player, Boolean>()
    val shopHolograms = Caffeine.newBuilder().asCache<Shop, List<Hologram>>()
    val renderDelay = 3.ticks

    val hologramManager: Display
        get() = plugin.hooks.display

    override suspend fun load() {
        plugin.launch { renderTask() }
    }

    override suspend fun kill() { }

    private suspend fun renderTask() {
        asyncScope {
        while (true) {
            for (player in server.onlinePlayers) {
                plugin.playerTracker.locations.getIfPresent(player)?.let {
                    render(player, it)
                }
            }
            delay(renderDelay)
        } }
    }

    private suspend fun render(player: Player, plot: Plot) { // Get centred starting location of hologram
        val closestShop = plot.shop.shops
            .mapNotNull { shops.getShop(it) }
            .minByOrNull { it.location.distanceSquared(player.location) } ?: return // No shop

        val startLoc = closestShop.location.add(0.5, 1.5, 0.5) // Location for text part of hologram

        if (startLoc.distanceSquared(player.location) >= shopConfig.viewDistance) { // Too far away
            far(player, closestShop, plot,  startLoc)
        }
        else {
            near(player, closestShop, plot, startLoc)
        }
    }

    private suspend fun far(player: Player, shop: Shop, plot: Plot, startLoc: Location) {
        if (active.getIfPresent(player) == false) { // It wasn't active to begin with
            return
        }

        active[player] = false // It was active, now it shouldn't be

        val tags = lang.getTags(player = player, shop = shop, plot = plot) // Get tags and replace
        val substitutedValues = shopConfig.display.map { lang.resolveTags(it, tags) }

        val holograms = shopHolograms.get(shop) {
            val textHologramData = TextHologramData("shop-text-${shop.shopId}", startLoc)
            textHologramData.text = substitutedValues
            val itemHologramData = ItemHologramData("shop-item-${shop.shopId}", startLoc.subtract(0.0, 0.75, 0.0))
            itemHologramData.itemStack = shop.ware
            listOf(
                hologramManager.createHologram(textHologramData),
                hologramManager.createHologram(itemHologramData)
            )
        }

        if (shop.location.block.type != Material.CHEST) {
            return
        }

        val chest = shop.location.block.state as Chest

        chest.close()

        for (hologram in holograms) {
            hologramManager.hideHologram(player, hologram) // Hide the hologram
        }
    }

    private suspend fun near(player: Player, shop: Shop, plot: Plot, startLoc: Location) {
        if (active.getIfPresent(player) == true) { // It was active to begin with
            return
        }

        active[player] = true // It was inactive, now it should be active as player is close

        val tags = lang.getTags(player = player, shop = shop, plot = plot) // Get tags and replace
        val substitutedValues = shopConfig.display.map { lang.resolveTags(it, tags) }

        val holograms = shopHolograms.get(shop) {
            val textHologramData = TextHologramData("shop-text-${shop.shopId}", startLoc)
            textHologramData.text = substitutedValues
            val itemHologramData = ItemHologramData("shop-item-${shop.shopId}", startLoc.subtract(0.0, 0.75, 0.0))
            itemHologramData.itemStack = shop.ware
            listOf(
                hologramManager.createHologram(textHologramData),
                hologramManager.createHologram(itemHologramData)
            )
        }

        if (shop.location.block.type != Material.CHEST) {
            return
        }

        val chest = shop.location.block.state as Chest

        chest.open()

        for (hologram in holograms) {
            hologramManager.showHologram(player, hologram)
        }
    }
}