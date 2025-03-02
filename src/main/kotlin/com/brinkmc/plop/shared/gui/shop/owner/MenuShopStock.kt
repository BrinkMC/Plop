package com.brinkmc.plop.shared.gui.shop.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    private val temporaryShop = mutableMapOf<Player, Shop>()
    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Pair<ShopType, >?>>() // Completable requests

    // Inventory items
    val SHOP_WARE: ItemStack = ItemStack(Material.BARRIER)

    // Less button
    val LESS: ItemStack = ItemStack(Material.REDSTONE_BLOCK)
        .name("shop.less-stock.name")
        .description("shop.less-stock.desc")

    // More button
    val MORE: ItemStack = ItemStack(Material.EMERALD_BLOCK)
        .name("shop.more-stock.name")
        .description("shop.more-stock.desc")

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 1

        val stockProperty = interfaceProperty(0)
        var stock by stockProperty

        withTransform(stockProperty) { pane, view ->
            val shop = temporaryShop[view.player] ?: return@withTransform
            stock = shop.stock
        }

        withTransform { pane, view ->

            val selectionPlot = receiverChoice[view.player]?.personalPlot() ?: return@withTransform

            val individualPersonalClone = PERSONAL_PLOT.clone().setSkull(selectionPlot.owner)

            pane[0, 5] = StaticElement(drawable(individualPersonalClone)) { (player) -> plugin.async {
                plotTypeChoice[player]?.complete(PlotType.PERSONAL)
                view.close()
            } }
        }

        withTransform { pane, view ->
            if (pane[0,3] == null && pane[0,5] == null) {
                view.close()
            }

            if (pane[0,3] == null && pane[0,5] != null) {
                plotTypeChoice[view.player]?.complete(PlotType.PERSONAL)
                view.close()
            }

            if (pane[0,3] != null && pane[0,5] == null) {
                plotTypeChoice[view.player]?.complete(PlotType.GUILD)
                view.close()
            }
        }

        addCloseHandler { reasons, handler  ->
            if (plotTypeChoice[handler.player]?.isCompleted == false) {
                plotTypeChoice[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    suspend fun requestChoice(player: Player, receiver: Player, plotType: PlotType? = null, parent: InterfaceView? = null): PlotType? {
        val personalPlot = receiver.personalPlot()
        val guildPlot = receiver.guildPlot()

        if (plotType != null) { // Handle already specified plot type
            when (plotType) {
                PlotType.PERSONAL -> { if (personalPlot == null) {
                    return null
                } }
                PlotType.GUILD -> { if (guildPlot == null) {
                    return null
                } }
            }
            return plotType
        }

        if ((personalPlot == null) && (guildPlot == null)) { // If no plots are available, return null
            return null
        }

        if ((personalPlot == null) xor (guildPlot == null)) { // If only one plot type is available, return that, no need to open menu
            return personalPlot?.let { PlotType.PERSONAL } ?: PlotType.GUILD
        }

        // Store the receiver and request for the plot type
        receiverChoice[player] = receiver
        val request = CompletableDeferred<PlotType?>()
        plotTypeChoice[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            plotTypeChoice.remove(player) // Remove the request because it's been fulfilled already
        }
    }
}