package com.brinkmc.plop.shared.gui.shop.init.price

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopBuy(override val plugin: Plop): Addon {

    // Base items initialized only once
    private object BaseItems {
        val BACK = ItemStack(Material.REDSTONE)
        val CONFIRM = ItemStack(Material.EMERALD)
        val MORE = ItemStack(Material.GOLD_INGOT)
        val LESS = ItemStack(Material.GOLD_NUGGET)
        val BAD = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shop = inputShop

        val unitMultiplierProperty = interfaceProperty(1)

        // Setup different sections of the interface
        setupConfirmButton(shop, unitMultiplierProperty)
        setupIncreaseButton(shop, unitMultiplierProperty)
        setupDecreaseButton(shop, unitMultiplierProperty)
        setupBackButton(shop)

        // Close handler logic
        addCloseHandler { _, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    private fun ChestInterfaceBuilder.setupConfirmButton(
        shop: Shop,
        unitMultiplierProperty: InterfaceProperty<Int>
    ) {
        withTransform(unitMultiplierProperty) { pane, view ->
            pane[1, 4] = if (shop.buyPrice <= 0.0f) {
                StaticElement(drawable(
                    BaseItems.BAD.get("shop.bad-amount.name", "shop.bad-amount.desc")
                ))
            } else {
                StaticElement(drawable(
                    BaseItems.CONFIRM.get("shop.confirm-stock.name", "shop.confirm-stock.desc")
                )) { (player) ->
                    plugin.async {
                        view.close()
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupIncreaseButton(
        shop: Shop,
        unitMultiplierProperty:InterfaceProperty<Int>
    ) {
        withTransform(unitMultiplierProperty) { pane, view ->
            var unitMultiplier by unitMultiplierProperty

            val multiplierPlaceholder = arrayOf(Placeholder.component("multiplier", Component.text(unitMultiplier)))

            pane[1, 6] = StaticElement(drawable(
                BaseItems.MORE.get("shop.more-amount.name", "shop.more-amount.desc", args = multiplierPlaceholder)
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

    private fun ChestInterfaceBuilder.setupDecreaseButton(
        shop: Shop,
        unitMultiplierProperty: InterfaceProperty<Int>
    ) {
        withTransform(unitMultiplierProperty) { pane, view ->
            var unitMultiplier by unitMultiplierProperty

            pane[1, 2] = if (0 < shop.buyPrice) {
                StaticElement(drawable(
                    BaseItems.LESS.get("shop.less-amount.name", "shop.less-amount.desc")
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
                    BaseItems.BAD.get("shop.bad-amount.toolittle.name", "shop.bad-amount.toolittle.desc")
                ))
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBackButton(
        shop: Shop
    ) {
        withTransform { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BaseItems.BACK.get("shop.back-stock.name", "shop.back-stock.desc")
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