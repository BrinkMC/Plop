package com.brinkmc.plop.shared.gui.shop.access.customer

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.TransactionResult
import com.brinkmc.plop.shared.util.message.ItemKey
import com.brinkmc.plop.shared.util.message.MessageKey
import com.brinkmc.plop.shared.util.message.SoundKey
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.plusAssign
import kotlin.compareTo
import kotlin.div
import kotlin.text.compareTo
import kotlin.text.set
import kotlin.times

class MenuBuy(override val plugin: Plop): Addon {

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
                lang.deserialise(MessageKey.BUY_MENU_TITLE, shop = shop, args = arrayOf(amountPlaceholder))
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

            // Customer is BUYING from the shop
            // Shop MUST have item, Client MUST have money being offered

            // Amount means if it is 2, and the shop is selling in quantities of 16x, then the client is buying 32x total
            pane[1, 6] = when(shops.transHandler.checkTransaction(view.player, shop, amount, ShopType.SELL)) {
                TransactionResult.FAILURE -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_ZERO_AMOUNT_NAME, MessageKey.MENU_ZERO_AMOUNT_DESC)
                    ))
                }
                TransactionResult.PLAYER_INSUFFICIENT_BALANCE -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_PLAYER_INSUFFICIENT_BALANCE_NAME, MessageKey.MENU_PLAYER_INSUFFICIENT_BALANCE_DESC)
                    ))
                }

                TransactionResult.SHOP_INSUFFICIENT_STOCK -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_SHOP_INSUFFICIENT_STOCK_NAME, MessageKey.MENU_SHOP_INSUFFICIENT_STOCK_DESC)
                    ))
                }

                TransactionResult.BUY_LIMIT_REACHED -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_BUY_LIMIT_REACHED_NAME, MessageKey.MENU_BUY_LIMIT_REACHED_DESC)
                    ))
                }
                TransactionResult.SUCCESS -> {
                    StaticElement(drawable(
                        ItemKey.GOOD.get(MessageKey.MENU_CONFIRM_BUY_NAME, MessageKey.MENU_CONFIRM_BUY_DESC)
                    )) { (player) ->
                        plugin.async {
                            val result = shops.transHandler.playerPurchase(player, shop, amount)
                            purchaseResult(player, shop, view, result)
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_ERROR_NAME, MessageKey.MENU_ERROR_DESC)
                    ))
                }
            }
        }
    }

    suspend fun purchaseResult(player: Player, shop: Shop, view: InterfaceView, transactionResult: TransactionResult) {
        when (transactionResult) {
            TransactionResult.PLAYER_INSUFFICIENT_BALANCE -> {
                player.sendMiniMessage(MessageKey.PLAYER_INSUFFICIENT_BALANCE)
                player.sendSound(SoundKey.FAILURE)
                view.redrawComplete()
            }
            TransactionResult.SHOP_INSUFFICIENT_STOCK -> {
                player.sendMiniMessage(MessageKey.PLAYER_INSUFFICIENT_STOCK)
                player.sendSound(SoundKey.FAILURE)
                view.redrawComplete()
            }
            TransactionResult.BUY_LIMIT_REACHED -> {
                player.sendMiniMessage(MessageKey.SHOP_BUY_LIMIT_REACHED)
                player.sendSound(SoundKey.FAILURE)
                view.redrawComplete()
            }
            TransactionResult.SUCCESS -> {
                player.sendMiniMessage(MessageKey.SHOP_PURCHASE_SUCCESSFUL)
                player.sendSound(SoundKey.BUY)
                view.close()
            }
            else -> { }
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

            val amountPlaceholder = arrayOf(
                Placeholder.component("amount", Component.text(amount))
            )

            pane[1, 3] = StaticElement(drawable(
                ItemKey.MORE.get(MessageKey.MENU_MORE_NAME, MessageKey.MENU_MORE_DESC, args = amountPlaceholder)
            )) { (_, _, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    amount += unitMultiplier
                    view.player.sendSound(SoundKey.CLICK)
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

            val amountPlaceholder = arrayOf( // Placeholder for the amount of items being bought
                Placeholder.component("amount", Component.text(amount))
            )

            pane[1, 1] = StaticElement(drawable(
                ItemKey.LESS.get(MessageKey.MENU_LESS_NAME, MessageKey.MENU_LESS_DESC, args = amountPlaceholder)
            )) { (_, _, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    amount = maxOf(1, amount - unitMultiplier)
                    view.player.sendSound(SoundKey.CLICK)
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