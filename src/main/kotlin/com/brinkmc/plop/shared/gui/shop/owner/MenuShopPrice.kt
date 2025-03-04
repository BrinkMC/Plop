package com.brinkmc.plop.shared.gui.shop.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
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

class MenuShopPrice(override val plugin: Plop): Addon {

    private val choiceOne = mutableMapOf<Player, Pair<ShopType, ItemStack>>()
    private val choiceTwo = mutableMapOf<Player, Pair<Int, Int>>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Float?>>() // Completable requests

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

        val priceProperty = interfaceProperty(0.0f)
        var price by priceProperty

        withTransform(priceProperty) { pane, view ->
            pane[0, 3] = if (price <= 0) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.price.less-one.name")
                )) { (player) -> plugin.async {
                    price -= 1
                } }
            }

            pane[0,5] = StaticElement(drawable(
                MORE.name("shop.price.more-one.name")
            )) { (player) -> plugin.async {
                price += 1
            } }

            pane[1, 3] = if (price <= 9) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.price.less-ten.name")
                )) { (player) -> plugin.async {
                    price -= 10
                } }
            }

            pane[1,5] = StaticElement(drawable(
                MORE.name("shop.price.more-ten.name")
            )) { (player) -> plugin.async {
                price += 10
            } }

            pane[2, 3] = if (price <= 100) { // No less button, instead indicator bad
                StaticElement(drawable(INDICATOR_BAD.name("shop.zero.name")))
            } else {
                StaticElement(drawable(
                    LESS.name("shop.price.less-hundred.name")
                )) { (player) -> plugin.async {
                    price -= 100
                } }
            }

            pane[2,5] = StaticElement(drawable(
                MORE.name("shop.price.more-hundred.name")
            )) { (player) -> plugin.async {
                price += 100
            } }

            view.title(lang.deserialise(
                "shop.menu.price",
                args = arrayOf(Placeholder.component("price", Component.text(price)))
            ))
        }

        withTransform { pane, view ->
            pane[1,8] = StaticElement(drawable(CONFIRM)) { (player) -> plugin.async {
                finalSelection[player]?.complete(price)
                view.close()
            } }
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

    suspend fun requestChoice(player: Player, c1: Pair<ShopType, ItemStack>, c2: Pair<Int, Int>, parent: InterfaceView? = null): Float? {
        resetFull(player)
        choiceOne[player] = c1 // Store previous choices
        choiceTwo[player] = c2

        val request = CompletableDeferred<Float?>()
        finalSelection[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            resetFull(player)
        }
    }

    fun resetFull(player: Player) {
        choiceOne.remove(player) // Remove the temporary shop
        choiceTwo.remove(player)
        finalSelection.remove(player) // Remove the request because it's been fulfilled already
    }
}