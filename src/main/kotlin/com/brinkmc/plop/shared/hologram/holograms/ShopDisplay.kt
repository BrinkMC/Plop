package com.brinkmc.plop.shared.hologram.holograms

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Display
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.dto.HologramWrapper
import com.brinkmc.plop.shared.hook.api.FancyHologram
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.brinkmc.plop.shop.dto.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import de.oliver.fancyholograms.api.data.ItemHologramData
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.util.UUID
import kotlin.text.compareTo

class ShopDisplay(override val plugin: Plop) : Display {

    override val playerHolograms = Caffeine.newBuilder().asCache<UUID, List<HologramWrapper>>()
    override val tickDelay = 3
    override val centreOffset = 0.5

    override var rendering: Boolean = true
    override var render: Job? = null

    override suspend fun findClosestMarker(playerId: UUID, plotId: UUID): Pair<Any, Double>? {
        val playerLocation = playerService.getLocation(playerId) ?: return null
        val shops: List<UUID> = shopService.getShops(plotId)

        return shops.mapNotNull { shopId ->
            val shopLocation = shopService.getShopLocation(shopId) ?: return@mapNotNull null
            val distance = shopLocation.add(centreOffset, 0.0, centreOffset).distanceSquared(playerLocation)
            if (distance < configService.shopConfig.viewDistance) {
                shopId to distance
            } else null
        }.minByOrNull { it.second }
    }

    override suspend fun getViewDistance(): Double = configService.shopConfig.viewDistance.toDouble()

    override suspend fun buildHolograms(playerId: UUID, plotId: UUID, marker: Any): List<HologramWrapper> {
        val shopId = marker as? UUID ?: return emptyList()

        val lines = configService.shopConfig.display
        val offset = lines.lastOrNull()?.substringBefore(":")?.substringAfter(",")?.toDoubleOrNull() ?: 0.0
        val itemOffset = lines.lastOrNull()?.substringBefore(":")?.substringBefore(",")?.toDoubleOrNull() ?: 0.0
        val scaleNumber = lines.lastOrNull()?.substringAfter(":")?.toFloatOrNull() ?: 1.0f
        val scale = Vector3f(scaleNumber, scaleNumber, scaleNumber)

        val textLocation = shopService.getShopLocation(shopId)?.apply { add(centreOffset, offset, centreOffset) }
            ?: return emptyList()

        val itemLocation = shopService.getShopLocation(shopId)?.apply { add(centreOffset, itemOffset, centreOffset) }
            ?: return emptyList()

        val textData = TextHologramData("shop-text-${shopId}", textLocation).apply { this.text = text }

        val shopItem = shopService.getShopItem(shopId) ?: return emptyList()
        val itemData = ItemHologramData("shop-item-${shopId}", itemLocation).apply {
            this.itemStack = shopItem
            this.scale = Vector3f(scale)
        }

        val textHologram = hologramService.createTextHologram(textData)
        val itemHologram = hologramService.createItemHologram(itemData)

        return listOf(
            HologramWrapper(shopId.toString(), textHologram, 1.5, Vector3f(1f, 1f, 1f)),
            HologramWrapper(shopId.toString(), itemHologram, 0.5, scale)
        )
    }

    override suspend fun doShowHologram(playerId: UUID, marker: Any) {
        val shopId = marker as? UUID ?: return
        val shopBlock = shopService.getShopLocation(shopId)?.block ?: return
        // Optionally open chest
        if (shopBlock.type == Material.CHEST) {
            plugin.sync {
                val chest = shopBlock.state as? Chest
                chest?.open()
            }
        }
    }

    override suspend fun doHideHologram(playerId: UUID, marker: Any) {
        val shopId = marker as? UUID ?: return
        val shopBlock = shopService.getShopLocation(shopId)?.block ?: return
        // Optionally open chest
        if (shopBlock.type == Material.CHEST) {
            plugin.sync {
                val chest = shopBlock.state as? Chest
                chest?.close()
            }
        }
    }

    override suspend fun load() {
        startRenderLoop()
    }

    override suspend fun kill() {
        stopRenderLoop()
    }
}

//class ShopDisplay(override val plugin: Plop): Addon, State {
//
//    private val active = Caffeine.newBuilder().asCache<Player, UUID?>()
//    val playerHolograms = Caffeine.newBuilder().asCache<Player, Pair<Hologram, Hologram>>()
//    val renderDelay = 3.ticks
//
//    // Constants for hologram positioning
//    companion object {
//        private const val TEXT_HEIGHT_OFFSET = 1.5
//        private const val ITEM_HEIGHT_OFFSET = 0.5
//        private const val CENTER_OFFSET = 0.5
//        private val HOLOGRAM_SCALE = Vector3f(0.5f, 0.5f, 0.5f)
//    }
//
//    val hologramManager: FancyHologram
//        get() = plugin.hooks.display
//
//    override suspend fun load() {
//        plugin.async { renderTask() }
//    }
//
//    override suspend fun kill() { }
//
//    private suspend fun renderTask() {
//        logger.info("Started render task for shops")
//        while (true) {
//            server.onlinePlayers.forEach { player ->
//                // Iterate over online players
//                val plot = plugin.playerTracker.locations.get(player) ?: return@forEach
//                render(player, plot)
//            }
//            delay(renderDelay)
//        }
//    }
//
//    private suspend fun render(player: Player, plot: Plot) {
//        // Find closest shop to player
//        val closestShop = plot.getShops()
//            .minByOrNull { it.location.clone().add(0.5, 0.0, 0.5).distanceSquared(player.location) } ?: return
//
//        val shopLoc = closestShop.location.clone()
//        val isWithinViewDistance = shopLoc.distanceSquared(player.location) < shopConfig.viewDistance
//
//        if (isWithinViewDistance) { // If the player is within view distance
//            near(player, closestShop, plot, shopLoc)
//        } else {
//            far(player, closestShop, plot, shopLoc)
//        }
//    }
//
//    private suspend fun far(player: Player, shop: Shop, plot: Plot, shopLoc: Location) {
//        // Only process if we need to deactivate
//        if (active.getIfPresent(player) == null) return
//
//        // Mark as inactive
//        active[player] = null
//        logger.info("Remove hologram")
//
//        // Get holograms and hide them
//        val holograms = playerHolograms.get(player) {
//            createHolograms(shop, shopLoc, getFormattedText(player, shop, plot))
//        }
//
//        // Close chest if it exists
//        tryCloseChest(shop.location)
//
//        // Hide holograms
//        hologramManager.hideHologram(player, holograms.first)
//        hologramManager.hideHologram(player, holograms.second)
//    }
//
//    private suspend fun near(player: Player, shop: Shop, plot: Plot, shopLoc: Location) {
//        // Skip if already showing this shop
//        if (active.getIfPresent(player) == shop.shopId) return
//
//        // Close any open chest first
//        tryCloseChest(active.getIfPresent(player)?.shop()?.location)
//
//        // Mark this shop as active for this player
//        active[player] = shop.shopId
//
//        // Get formatted text for display
//        val substitutedValues = getFormattedText(player, shop, plot)
//
//        // Get or create holograms
//        val holograms = playerHolograms.get(player) {
//            createHolograms(shop, shopLoc, substitutedValues)
//        }
//
//        // Open the chest if it exists
//        tryOpenChest(shop.location)
//
//        // Update and show holograms
//        val updated = updateHolograms(holograms, shop, shopLoc, substitutedValues)
//        hologramManager.showHologram(player, updated.first)
//        hologramManager.showHologram(player, updated.second)
//    }
//
//    private fun getFormattedText(player: Player, shop: Shop, plot: Plot): List<String> {
//        val tags = lang.getTags(player = player, shop = shop, plot = plot)
//        return shopConfig.display.map { lang.resolveTags(it, tags) }
//    }
//
//    private suspend fun tryOpenChest(location: Location) {
//        if (location.block.type != Material.CHEST) return
//
//        syncScope {
//            val chest = location.block.state as? Chest ?: return@syncScope
//            chest.open()
//        }
//    }
//
//    private suspend fun tryCloseChest(location: Location?) {
//        if (location?.block?.type != Material.CHEST) return
//
//        syncScope {
//            val chest = location.block.state as? Chest ?: return@syncScope
//            chest.close()
//        }
//    }
//
//    fun createHolograms(shop: Shop, shopLoc: Location, text: List<String>): Pair<Hologram, Hologram> {
//        val centerLoc = shopLoc.clone().add(CENTER_OFFSET, TEXT_HEIGHT_OFFSET, CENTER_OFFSET)
//        val itemLoc = centerLoc.clone().subtract(0.0, ITEM_HEIGHT_OFFSET, 0.0)
//
//        // Create text hologram
//        val textHologramData = TextHologramData("shop-text-${shop.shopId}", centerLoc)
//        textHologramData.text = text
//
//        // Create item hologram
//        val itemHologramData = ItemHologramData("shop-item-${shop.shopId}", itemLoc)
//        itemHologramData.itemStack = shop.item
//        itemHologramData.scale = HOLOGRAM_SCALE
//
//        return Pair(
//            hologramManager.createHologram(textHologramData),
//            hologramManager.createHologram(itemHologramData)
//        )
//    }
//
//    fun updateHolograms(pair: Pair<Hologram, Hologram>, shop: Shop, shopLoc: Location, text: List<String>): Pair<Hologram, Hologram> {
//        val centerLoc = shopLoc.clone().add(CENTER_OFFSET, TEXT_HEIGHT_OFFSET, CENTER_OFFSET)
//        val itemLoc = centerLoc.clone().subtract(0.0, ITEM_HEIGHT_OFFSET, 0.0)
//
//        // Update text hologram
//        val textHologram = pair.first
//        val textData = textHologram.data as TextHologramData
//        textData.text = text
//        textHologram.data.setLocation(centerLoc)
//
//        // Update item hologram
//        val itemHologram = pair.second
//        val itemData = itemHologram.data as ItemHologramData
//        itemData.itemStack = shop.item
//        itemData.scale = HOLOGRAM_SCALE
//        itemHologram.data.setLocation(itemLoc)
//
//        return pair
//    }
//}