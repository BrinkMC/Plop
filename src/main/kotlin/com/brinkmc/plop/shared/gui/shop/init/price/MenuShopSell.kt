package com.brinkmc.plop.shared.gui.shop.init.price

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.*
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopSell(override val plugin: Plop): Addon {

    private object BaseItems {
        val BACK = ItemStack(Material.REDSTONE)
        val CONFIRM = ItemStack(Material.EMERALD)
        val MORE = ItemStack(Material.GOLD_INGOT)
        val LESS = ItemStack(Material.GOLD_NUGGET)
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

        val unitMultiplierProperty = interfaceProperty(1)

        setupConfirmButton(shopProperty, unitMultiplierProperty)
        setupBackButton(shopProperty)
        setupMoreButton(shopProperty, unitMultiplierProperty)
        setupLessButton(shopProperty, unitMultiplierProperty)

        addCloseHandler { reasons, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    private fun ChestInterfaceBuilder.setupMoreButton(shopProperty: InterfaceProperty<Shop>, unitMultiplierProperty: InterfaceProperty<Int>) {
        // Increase
        var unitMultiplier by unitMultiplierProperty
        var shop by shopProperty
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->

            val multiplierPlaceholder = Placeholder.component("multiplier", Component.text(unitMultiplier))

            pane[1, 6] = StaticElement(drawable(
                getItem(BaseItems.MORE, "shop.more-amount.name", "shop.more-amount.desc", args = arrayOf(multiplierPlaceholder))
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    shop.setSellPrice(shop.sellPrice + unitMultiplier)
                    view.redrawComplete()
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupLessButton(shopProperty: InterfaceProperty<Shop>, unitMultiplierProperty: InterfaceProperty<Int>) {
        // Less button
        var unitMultiplier by unitMultiplierProperty
        var shop by shopProperty
        withTransform(shopProperty) { pane, view ->
            pane[1, 2] = if (0 < shop.sellPrice) {
                StaticElement(drawable(
                    getItem(BaseItems.LESS, "shop.less-amount.name", "shop.less-amount.desc")
                )) { (player, view, click) ->
                    plugin.async {
                        if (click.isDrop()) {
                            unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                            return@async
                        }
                        val finalPrice = if (shop.sellPrice - unitMultiplier < 0) 0.0f else shop.sellPrice - unitMultiplier
                        shop.setSellPrice(finalPrice)
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    getItem(BaseItems.BAD, "shop.bad-amount.toolittle.name", "shop.bad-amount.toolittle.desc")
                )) // Do nothing
            }
        }
    }

    private fun ChestInterfaceBuilder.setupConfirmButton(shopProperty: InterfaceProperty<Shop>, unitMultiplierProperty: InterfaceProperty<Int>) {
        // Confirm
        var shop by shopProperty

        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->
            pane[1, 4] = if (shop.sellPrice <= 0.0f) {
                StaticElement(drawable(
                    getItem(BaseItems.BAD, "shop.bad-amount.toosmall.name", "shop.bad-amount.toosmall.desc")
                ))
            } else {
                StaticElement(drawable(
                    getItem(BaseItems.CONFIRM, "shop.confirm", "shop.confirm.desc")
                )) { (player) ->
                    plugin.async {
                        view.close()
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBackButton(shopProperty: InterfaceProperty<Shop>) {
        // Back button
        withTransform(shopProperty) { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                getItem(BaseItems.BACK, "menu.back.name", "menu.back.desc")
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