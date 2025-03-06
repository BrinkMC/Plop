package com.brinkmc.plop.shared.gui.shop.init.price

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class MenuShopBuy(override val plugin: Plop): Addon {

    // Base items initialized only once
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

    private fun inventory(player: Player, inputShop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        val unitMultiplierProperty = interfaceProperty(1)

        // Setup different sections of the interface
        setupConfirmButton(shopProperty, unitMultiplierProperty)
        setupIncreaseButton(shopProperty, unitMultiplierProperty)
        setupDecreaseButton(shopProperty, unitMultiplierProperty)
        setupBackButton(shopProperty)

        // Close handler logic
        addCloseHandler { _, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    private fun com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder.setupConfirmButton(
        shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>,
        unitMultiplierProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->
            val shop by shopProperty

            pane[1, 4] = if (shop.buyPrice <= 0.0f) {
                StaticElement(drawable(
                    getItem(BaseItems.BAD, "shop.bad-amount.name", "shop.bad-amount.desc")
                ))
            } else {
                StaticElement(drawable(
                    getItem(BaseItems.CONFIRM, "shop.confirm-stock.name", "shop.confirm-stock.desc")
                )) { (player) ->
                    plugin.async {
                        view.close()
                    }
                }
            }
        }
    }

    private fun com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder.setupIncreaseButton(
        shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>,
        unitMultiplierProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->
            var shop by shopProperty
            var unitMultiplier by unitMultiplierProperty

            val multiplierPlaceholder = Placeholder.component("multiplier", Component.text(unitMultiplier))

            pane[1, 6] = StaticElement(drawable(
                getItem(BaseItems.MORE, "shop.more-amount.name", "shop.more-amount.desc", multiplierPlaceholder)
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    shop.setBuyPrice(shop.buyPrice + unitMultiplier)
                    view.redrawComplete()
                }
            }
        }
    }

    private fun com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder.setupDecreaseButton(
        shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>,
        unitMultiplierProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Int>
    ) {
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->
            var shop by shopProperty
            var unitMultiplier by unitMultiplierProperty

            pane[1, 2] = if (0 < shop.buyPrice) {
                StaticElement(drawable(
                    getItem(BaseItems.LESS, "shop.less-amount.name", "shop.less-amount.desc")
                )) { (player, view, click) ->
                    plugin.async {
                        if (click.isDrop()) {
                            unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                            return@async
                        }
                        val finalPrice = if (shop.buyPrice - unitMultiplier < 0) 0.0f else shop.buyPrice - unitMultiplier
                        shop.setBuyPrice(finalPrice)
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    getItem(BaseItems.BAD, "shop.bad-amount.toolittle.name", "shop.bad-amount.toolittle.desc")
                ))
            }
        }
    }

    private fun com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder.setupBackButton(
        shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>
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