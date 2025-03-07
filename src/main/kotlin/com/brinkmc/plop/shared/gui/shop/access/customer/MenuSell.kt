package com.brinkmc.plop.shared.gui.shop.access.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuSell(override val plugin: Plop): Addon {

    // Base items initialized only once
    private object BaseItems {
        val CONFIRM = ItemStack(Material.EMERALD)
        val MORE = ItemStack(Material.GOLD_INGOT)
        val LESS = ItemStack(Material.GOLD_NUGGET)
        val BACK = ItemStack(Material.REDSTONE)
        val BAD = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    // Helper function to get named and described items
    private fun getItem(baseItem: ItemStack, nameKey: String? = null, descKey: String? = null, vararg args: TagResolver): ItemStack {
        var item = baseItem.clone()
        if (nameKey != null) {
            item = item.name(nameKey, args = args)
        }
        if (descKey != null) {
            item = item.description(descKey, args = args)
        }
        return item
    }

    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        val shop by shopProperty

        val amountProperty = interfaceProperty(0)
        var amount by amountProperty

        // Setup different sections of the interface
        setupConfirmButton(shopProperty, amountProperty)
        setupIncreaseButton(shopProperty, amountProperty)
        setupDecreaseButton(shopProperty, amountProperty)
        setupBackButton(shopProperty)

        withTransform { pane, view ->
            // Shop items display
            pane[1, 2] = StaticElement(drawable(shop.item))
        }

        // Close handler logic
        addCloseHandler { _, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    private fun ChestInterfaceBuilder.setupConfirmButton(
        shopProperty: InterfaceProperty<Shop>,
        amountProperty: InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, amountProperty) { pane, view ->
            val shop by shopProperty
            var amount by amountProperty

            // Customer is BUYING from the shop
            // Shop MUST have item, Client MUST have money being offered

            // Amount means if it is 2, and the shop is selling in quantities of 16x, then the client is buying 32x total
            pane[1, 6] = when {
                amount <= 0 -> { // You aren't buying anything
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.bad-amount.name", "shop.bad-amount.desc")
                    ))
                }
                amount * shop.sellPrice > economy.getBalance(view.player) -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.bad-amount.client-bad.name", "shop.bad-amount.client-bad.desc")
                    ))
                }
                amount > shop.quantity/shop.item.amount -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.bad-amount.shop-bad.name", "shop.bad-amount.shop-bad.desc")
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        getItem(BaseItems.CONFIRM, "shop.confirm-stock.name", "shop.confirm-stock.desc")
                    )) { (player) ->
                        plugin.async {
                            shop.doTransaction(player, amount, ShopType.SELL, economy)
                            view.close()
                        }
                    }
                }
            }
        }
    }

    // Increase the amount of items being bought
    private fun ChestInterfaceBuilder.setupIncreaseButton(
        shopProperty: InterfaceProperty<Shop>,
        amountProperty: InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, amountProperty) { pane, view ->
            var shop by shopProperty
            var amount by amountProperty

            val amountPlaceholder = Placeholder.component("amount", Component.text(amount))

            pane[1, 3] = StaticElement(drawable(
                getItem(BaseItems.MORE, "shop.more-amount.name", "shop.more-amount.desc", amountPlaceholder)
            )) { (player, view, click) ->
                plugin.async {
                    amount += 1
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupDecreaseButton(
        shopProperty: InterfaceProperty<Shop>,
        amountProperty: InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, amountProperty) { pane, view ->
            var shop by shopProperty
            var amount by amountProperty

            val amountPlaceholder = Placeholder.component("amount", Component.text(amount))

            pane[1, 1] = StaticElement(drawable(
                getItem(BaseItems.LESS, "shop.less-amount.name", "shop.less-amount.desc", amountPlaceholder)
            )) { (player, view, click) ->
                plugin.async {
                    amount = maxOf(0, amount - 1)
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBackButton(
        shopProperty: InterfaceProperty<Shop>
    ) {
        withTransform(shopProperty) { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                getItem(BaseItems.BACK, "shop.back-stock.name", "shop.back-stock.desc")
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): InterfaceView {
        return inventory(player, shop).open(player, parentView)
    }
}