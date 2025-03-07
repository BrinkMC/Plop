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
import com.sksamuel.aedile.core.asLoadingCache
import de.oliver.fancyholograms.api.data.ItemHologramData
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.joml.Vector3f

class ShopDisplay(override val plugin: Plop): Addon, State {

    private val active = Caffeine.newBuilder().asCache<Player, Location?>()
    val playerHolograms = Caffeine.newBuilder().asCache<Player, Pair<Hologram, Hologram>>()
    val renderDelay = 3.ticks

    val hologramManager: Display
        get() = plugin.hooks.display

    override suspend fun load() {
        plugin.async { renderTask() }
    }

    override suspend fun kill() { }

    private suspend fun renderTask() {
        logger.info("Started render task for shops")
        while (true) {
            for (player in server.onlinePlayers) {
                val loc = plugin.playerTracker.locations.get(player) ?: continue
                render(player, loc)
            }
            delay(renderDelay)
        }
    }

    private suspend fun render(player: Player, plot: Plot) { // Get centred starting location of hologram
        val closestShop = plot.getShops()
            .minByOrNull { it.location.distanceSquared(player.location) } ?: return // No shop

        val startLoc = closestShop.location.clone().add(0.5, 1.5, 0.5) // Location for text part of hologram

        if (startLoc.distanceSquared(player.location) >= shopConfig.viewDistance) { // Too far away
            far(player, closestShop, plot,  startLoc)
        }
        else {
            near(player, closestShop, plot, startLoc)
        }
    }

    private suspend fun far(player: Player, shop: Shop, plot: Plot, startLoc: Location) {
        if (active.getIfPresent(player) == null) { // It was inactive to begin with
            return
        }

        active[player] = null // It should be inactive
        logger.info("Remove hologram")

        val tags = lang.getTags(player = player, shop = shop, plot = plot) // Get tags and replace
        val substitutedValues = shopConfig.display.map { lang.resolveTags(it, tags) }

        val holograms = playerHolograms.get(player) { setHolograms(shop, startLoc, substitutedValues) }

        if (shop.location.block.type != Material.CHEST) {
            return
        }

        syncScope {
            val chest = shop.location.block.state as Chest
            chest.close()
        }

        val hologramOne = holograms.first
        val hologramTwo = holograms.second

        hologramManager.hideHologram(player, hologramOne)
        hologramManager.hideHologram(player, hologramTwo)
    }

    private suspend fun near(player: Player, shop: Shop, plot: Plot, startLoc: Location) {
        if (active.getIfPresent(player) == startLoc) { // It was active to begin with
            return
        }

        active[player] = startLoc // It was inactive, now it should be active as player is close

        val tags = lang.getTags(player = player, shop = shop, plot = plot) // Get tags and replace
        val substitutedValues = shopConfig.display.map { lang.resolveTags(it, tags) }

        playerHolograms[player] = setHolograms(shop, startLoc, substitutedValues) // IT MUST BE A NEW SHOP?

        val holograms = playerHolograms.get(player) {
            setHolograms(shop, startLoc, substitutedValues)
        }

        if (shop.location.block.type != Material.CHEST) {
            return
        }


        syncScope {
            val chest = shop.location.block.state as Chest
            chest.open()
        }
        val hologramOne = holograms.first
        val hologramTwo = holograms.second

        hologramOne.data.setLocation(startLoc)
        hologramTwo.data.setLocation(startLoc.clone().subtract(0.0, 0.5, 0.0))
        hologramManager.showHologram(player, hologramOne)
        hologramManager.showHologram(player, hologramTwo)
    }

    fun setHolograms(shop: Shop, startLoc: Location, substitutedValues: List<String> ): Pair<Hologram, Hologram> {
        val textHologramData = TextHologramData("shop-text-${shop.shopId}", startLoc)
        textHologramData.text = substitutedValues
        val itemHologramData = ItemHologramData("shop-item-${shop.shopId}", startLoc.clone().subtract(0.0, 0.5, 0.0))
        itemHologramData.itemStack = shop.item
        itemHologramData.scale = Vector3f(0.5f, 0.5f, 0.5f)
        return Pair(
            hologramManager.createHologram(textHologramData),
            hologramManager.createHologram(itemHologramData)
        )
    }
}