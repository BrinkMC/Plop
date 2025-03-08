package com.brinkmc.plop.shared.gui.shop.access.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.handler.ShopTransactionHandler
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

    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shop = inputShop

        val amountProperty = interfaceProperty(1)
        var amount by amountProperty

        val unitMultiplierProperty = interfaceProperty(1)
        var unitMultiplier by unitMultiplierProperty

        // Setup different sections of the interface
        setupConfirmButton(shop, amountProperty)
        setupIncreaseButton(shop, amountProperty, unitMultiplierProperty)
        setupDecreaseButton(shop, amountProperty, unitMultiplierProperty)
        setupBackButton(shop)

        withTransform(amountProperty, unitMultiplierProperty) { pane, view ->
            // Shop items display
            val clone = shop.item.clone()
            clone.amount = amount * shop.item.amount
            pane[1, 2] = StaticElement(drawable(
                clone
            ))

            // Placeholders
            val amountPlaceholder = Placeholder.component("amount", Component.text(amount))

            // Shop title
            view.title(
                lang.deserialise("shop.sell-player.title", shop = shop, args = arrayOf(amountPlaceholder))
            )
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
        shop: Shop,
        amountProperty: InterfaceProperty<Int>
    ) {
        withTransform(amountProperty) { pane, view ->
            var amount by amountProperty

            // Customer is SELLING to the shop
            // Shop MUST afford, Client MUST have item being offered

            // Amount means if it is 2, and the shop is selling in quantities of 16x, then the client is buying 32x total
            pane[1, 6] = when(shops.transHandler.checkTransaction(view.player, shop, amount, ShopType.BUY)) {
                ShopTransactionHandler.TransactionResult.FAILURE -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.bad-amount.client-bad.name", "shop.bad-amount.client-bad.desc")
                    ))
                }
                ShopTransactionHandler.TransactionResult.PLAYER_INSUFFICIENT_STOCK -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.bad-amount.client-bad.name", "shop.bad-amount.client-bad.desc")
                    ))
                }

                ShopTransactionHandler.TransactionResult.SHOP_INSUFFICIENT_BALANCE -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.bad-amount.shop-bad.name", "shop.bad-amount.shop-bad.desc")
                    ))
                }

                ShopTransactionHandler.TransactionResult.BUY_LIMIT_REACHED -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.bad-amount.buy-limit.name", "shop.bad-amount.buy-limit.desc")
                    ))
                }

                ShopTransactionHandler.TransactionResult.SUCCESS -> {
                    StaticElement(drawable(
                        BaseItems.CONFIRM.get("shop.confirm-stock.name", "shop.confirm-stock.desc")
                    )) { (player) ->
                        plugin.async {
                            val result = shops.transHandler.playerSell(player, shop, amount)
                            player.sendMiniMessage(result.toString())
                            view.close()
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("error", "error")
                    ))
                }
            }
        }
    }

    // Increase the amount of items being bought
    private fun ChestInterfaceBuilder.setupIncreaseButton(
        shop: Shop,
        amountProperty: InterfaceProperty<Int>,
        unitMultiplierProperty: InterfaceProperty<Int>
    ) {
        withTransform(amountProperty, unitMultiplierProperty) { pane, view ->
            var amount by amountProperty
            var unitMultiplier by unitMultiplierProperty

            val amountPlaceholder = arrayOf(Placeholder.component("amount", Component.text(amount)))

            pane[1, 3] = StaticElement(drawable(
                BaseItems.MORE.get("shop.more-amount.name", "shop.more-amount.desc", args = amountPlaceholder)
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    amount += unitMultiplier
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupDecreaseButton(
        shop: Shop,
        amountProperty: InterfaceProperty<Int>,
        unitMultiplierProperty: InterfaceProperty<Int>
    ) {
        withTransform(amountProperty, unitMultiplierProperty) { pane, view ->
            var amount by amountProperty
            var unitMultiplier by unitMultiplierProperty

            val amountPlaceholder = arrayOf(Placeholder.component("amount", Component.text(amount)))

            pane[1, 1] = StaticElement(drawable(
                BaseItems.LESS.get("shop.less-amount.name", "shop.less-amount.desc", args = amountPlaceholder)
            )) { (_, _, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    maxOf(1, amount - unitMultiplier)
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBackButton(
        shop: Shop
    ) {
        withTransform { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BaseItems.BACK.get("shop.back-stock.name", "shop.back-stock.desc")
            )) { (_) ->
                plugin.async {
                    view.close()
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): InterfaceView {
        val view = inventory(player, shop).open(player, parentView)
        view.redrawComplete()
        return view
    }
}