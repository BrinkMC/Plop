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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    private val choiceOne = mutableMapOf<Player, Pair<ShopType, ItemStack>>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Pair<Int, Int>?>>() // Completable requests

    // Less button
    val LESS: ItemStack
        get() = ItemStack(Material.REDSTONE_BLOCK)
        .name("shop.less-stock.name")
        .description("shop.less-stock.desc")

    // More button
    val MORE: ItemStack
        get() = ItemStack(Material.EMERALD_BLOCK)
        .name("shop.more-stock.name")
        .description("shop.more-stock.desc")

    val CONFIRM
        get() = ItemStack(Material.EMERALD)
        .name("shop.confirm-stock.name")
        .description("shop.confirm-stock.desc")

    val INDICATOR_BAD: ItemStack = ItemStack(Material.RED_CONCRETE)

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 3

        val stockProperty = interfaceProperty(0)
        var stock by stockProperty

        val stockLimitProperty = interfaceProperty(0)
        var stockLimit by stockLimitProperty

        withTransform(stockProperty, stockLimitProperty) { pane, view ->
            val player = view.player
            val shopChoiceOne = choiceOne[player] ?: return@withTransform

            if (shopChoiceOne.first == ShopType.BUY) { return@withTransform } // This is a sell shop render

            pane[0, 3] = if (stock <= 0) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.less-one.name")
                )) { (player) -> plugin.async {
                    stock -= 1
                } }
            }

            pane[0,5] = if (stock >= view.player.inventory.getAmountOf(shopChoiceOne.second)) {
                StaticElement(drawable(INDICATOR_BAD.name("shop.too-much.name"))) // Too much stock for amount in inventory currently
            } else {
                StaticElement(drawable(
                    MORE.name("shop.more-one.name")
                )) { (player) -> plugin.async {
                    stock += 1
                } }
            }

            pane[1, 3] = if (stock <= 9) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.less-ten.name")
                )) { (player) -> plugin.async {
                    stock -= 10
                } }
            }

            pane[1,5] = if ((stock + 10) >= view.player.inventory.getAmountOf(shopChoiceOne.second)) {
                StaticElement(drawable(INDICATOR_BAD.name("shop.too-much.name"))) // Too much stock for amount in inventory currently
            } else {
                StaticElement(drawable(
                    MORE.name("shop.more-ten.name")
                )) { (player) -> plugin.async {
                    stock += 10
                } }
            }

            pane[2, 3] = if (stock <= 99) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.less-hundred.name")
                )) { (player) -> plugin.async {
                    stock -= 100
                } }
            }

            pane[2,5] = if ((stock + 100) >= view.player.inventory.getAmountOf(shopChoiceOne.second)) {
                StaticElement(drawable(INDICATOR_BAD.name("shop.too-much.name"))) // Too much stock for amount in inventory currently
            } else {
                StaticElement(drawable(
                    MORE.name("shop.more-hundred.name")
                )) { (player) -> plugin.async {
                    stock += 100
                } }
            }
        }

        withTransform(stockProperty, stockLimitProperty) { pane, view ->
            val player = view.player
            val shopChoiceOne = choiceOne[player] ?: return@withTransform

            if (shopChoiceOne.first == ShopType.SELL) { return@withTransform } // This is a buy shop render

            pane[0, 3] = if (stockLimit <= 0) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(LESS)) { (player) -> plugin.async {
                    stockLimit -= 1
                } }
            }

            pane[0,5] = StaticElement(drawable(MORE)) { (player) -> plugin.async {
                stockLimit += 1
            } }

            pane[1, 3] = if (stockLimit <= 9) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(LESS)) { (player) -> plugin.async {
                    stockLimit -= 10
                } }
            }

            pane[1,5] = StaticElement(drawable(MORE)) { (player) -> plugin.async {
                stockLimit += 10
            } }

            pane[2, 3] = if (stockLimit <= 99) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(LESS)) { (player) -> plugin.async {
                    stockLimit -= 100
                } }
            }

            pane[2,5] = StaticElement(drawable(MORE)) { (player) -> plugin.async {
                stockLimit += 100
            } }
        }

        // Confirm button
        withTransform { pane, view ->
            val shopChoiceOne = choiceOne[view.player] ?: return@withTransform

            when (shopChoiceOne.first) { // Don't confirm if stock is empty
                ShopType.SELL -> if (stock <= 0) { return@withTransform }
                ShopType.BUY -> if (stockLimit <= 0) { return@withTransform }
            }

            pane[1, 8] = StaticElement(drawable(CONFIRM)) { (player) -> plugin.async {
                finalSelection[player]?.complete(Pair(stock, stockLimit))
                view.close()
            } }
        }

        withTransform(stockProperty, stockLimitProperty) { pane, view ->
            val shopChoiceOne = choiceOne[view.player] ?: return@withTransform

            when (shopChoiceOne.first) {
                ShopType.SELL -> view.title(lang.deserialise(
                    "shop.menu.stock",
                    args = arrayOf(Placeholder.component("stock", Component.text(stock)))
                ))
                ShopType.BUY -> view.title(lang.deserialise(
                    "shop.menu.stock",
                    args = arrayOf(Placeholder.component("stock", Component.text(stockLimit)))
                ))
            }
        }

        addCloseHandler { reasons, handler  ->
            if (finalSelection[handler.player]?.isCompleted == false) {
                finalSelection[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            resetFull(handler.player)

            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    suspend fun requestChoice(player: Player, c1: Pair<ShopType, ItemStack>, parent: InterfaceView? = null): Pair<Int, Int>? {
        resetFull(player)
        choiceOne[player] = c1 // Store previous choices

        val request = CompletableDeferred<Pair<Int, Int>?>()
        finalSelection[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            resetFull(player)
        }
    }

    fun resetFull(player: Player) {
        choiceOne.remove(player)
        finalSelection.remove(player)
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