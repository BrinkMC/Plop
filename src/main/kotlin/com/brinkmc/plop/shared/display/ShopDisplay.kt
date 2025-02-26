package com.brinkmc.plop.shared.display

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shop.shop.Shop
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.sksamuel.aedile.core.asCache
import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player

class ShopDisplay(override val plugin: Plop): Addon, State {

    val shopHolograms = Caffeine.newBuilder().asCache<Player, Hologram>()
    val plotShops = Caffeine.newBuilder().asCache<Plot, List<Location>>()
    val renderDelay = 2.ticks

    override suspend fun load() {
        renderTask()
    }

    override suspend fun kill() { }

    private suspend fun renderTask() {
        while (true) { asyncScope {
            for (player in server.onlinePlayers) {
                plugin.playerTracker.locations.getIfPresent(player)?.let {
                    render(player, it)
                }
            }
            delay(renderDelay)
        } }
    }

    private suspend fun render(player: Player, plot: Plot) { // Get centred starting location of hologram
        val shops = plotShops.get(plot) {
            plot.shop.shops.mapNotNull { shops.getShop(it)?.location }
        }

        val startLoc = shops.getClosest(player.location)?.add(0.5, 1.5, 0.5) ?: return // No nexus
        if (startLoc.distanceSquared(player.location) >= shopConfig.viewDistance) { // Too far away
            far(player)
        }
        else {
            // near(player, plot, startLoc)
        }
    }

    private suspend fun far(player: Player) {
        val potentialHologram = shopHolograms.getIfPresent(player) ?: return // Is there a hologram?
        if (!potentialHologram.isVisible(player)) { // There is hologram check if visible
            return
        }
        potentialHologram.removeShowPlayer(player) // It shouldn't be visible anymore
    }

//    private suspend fun near(player: Player, shop: Shop, location: Location) {
//        val substitutedValues = plotConfig.nexusConfig.display.map {
//            it.replace("<owner>", shop)
//            it.replace("<item>", )
//        }
//        val potentialHologram = shopHolograms.get(player) {
//            val create = DHAPI.createHologram("nexus-${player.uniqueId}", location, false, substitutedValues)
//            create.isDefaultVisibleState = false
//            create
//        }
//        // Teleport the hologram to the new location (should be the same text e.t.c)
//        potentialHologram.location = location
//    }
}