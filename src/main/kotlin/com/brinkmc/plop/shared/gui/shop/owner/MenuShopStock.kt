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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    private val choiceOne = mutableMapOf<Player, Pair<ShopType, ItemStack>>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Pair<Int, Int>?>>() // Completable requests

    // Less button
    val LESS: ItemStack = ItemStack(Material.REDSTONE_BLOCK)
        .name("shop.less-stock.name")
        .description("shop.less-stock.desc")

    // More button
    val MORE: ItemStack = ItemStack(Material.EMERALD_BLOCK)
        .name("shop.more-stock.name")
        .description("shop.more-stock.desc")

    val INDICATOR_BAD: ItemStack = ItemStack(Material.RED_CONCRETE)

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 3

        val stockProperty = interfaceProperty(0)
        var stock by stockProperty

        val stockLimitProperty = interfaceProperty(0)
        var stockLimit by stockLimitProperty

        withTransform(stockProperty) { pane, view ->
            val shop = temporaryShop[view.player] ?: return@withTransform
            stock = shop.stock

            pane[0, 3] = if (stock <= 0) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(LESS)) { (player) -> plugin.async {
                    stock -= 1
                    temporaryShop[player]?.stock = stock
                } }
            }

            pane[0,5] = if (stock >= view.player.inventory.getAmountOf(temporaryShop[view.player].ware))
        }

        addCloseHandler { reasons, handler  ->
            if (finalSelection[handler.player]?.isCompleted == false) {
                finalSelection[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            choiceOne.remove(handler.player) // Remove the temporary shop
            finalSelection.remove(handler.player) // Remove the final selection

            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    suspend fun requestChoice(player: Player, c1: Pair<ShopType, ItemStack>, parent: InterfaceView? = null): Pair<Int, Int>? {
        choiceOne[player] = c1 // Store previous choices
        val request = CompletableDeferred<Pair<Int, Int>?>()
        finalSelection[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            choiceOne.remove(player) // Remove the temporary shop
            finalSelection.remove(player) // Remove the request because it's been fulfilled already
        }
    }

    private fun Inventory.getAmountOf(item: ItemStack): Int {
        var amount = 0
        for (bigItem in this.contents) {
            if (bigItem == null) {
                continue
            }
            if (bigItem.isSimilar(item)) {
                amount += bigItem.amount
            }
        }
        return amount
    }
}