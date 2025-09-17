package com.brinkmc.plop.shop.controller.gui

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Gui
import com.brinkmc.plop.shared.design.enums.ItemKey
import com.brinkmc.plop.shared.design.enums.MessageKey
import com.brinkmc.plop.shared.design.enums.SoundKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shop.constant.ShopType
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

internal interface ShopGui: Gui {



    suspend fun getIncrementUpButton(): ItemStack {
        return messages.getItem(
            ItemKey.INCREMENT_UP,
            MessageKey.MENU_SHOP_INCREMENT_UP_NAME,
            MessageKey.MENU_SHOP_INCREMENT_UP_DESC
        )
    }

    suspend fun getIncrementDownButton(): ItemStack {
        return messages.getItem(
            ItemKey.INCREMENT_DOWN,
            MessageKey.MENU_SHOP_INCREMENT_DOWN_NAME,
            MessageKey.MENU_SHOP_INCREMENT_DOWN_DESC
        )
    }

    suspend fun getIncrementCycleButton(): ItemStack {
        return messages.getItem(
            ItemKey.INCREMENT_CYCLE,
            MessageKey.MENU_SHOP_INCREMENT_CYCLE_NAME,
            MessageKey.MENU_SHOP_INCREMENT_CYCLE_DESC
        )
    }

    suspend fun getConfirmButton(): ItemStack {
        return messages.getItem(
            ItemKey.PURCHASE,
            MessageKey.MENU_SHOP_PURCHASE_NAME,
            MessageKey.MENU_SHOP_PURCHASE_DESC
        )
    }

    suspend fun ChestInterfaceBuilder.setupIncrementButtons() {
        withTransform { pane, view ->
            val playerId = view.player.uniqueId
            val shopId = shopAccessService.getViewedShop(playerId) ?: return@withTransform
            val shopType = shopService.getShopType(shopId) ?: return@withTransform

            pane[0, 2] = StaticElement(
                drawable(
                    getIncrementUpButton()
                )
            ) { _ ->
                plugin.async {
                    when (shopAccessService.increment(playerId)) {
                        true -> { // Successfully incremented
                            messages.sendMiniMessage(view.player, MessageKey.SHOP_INCREMENT_UP)
                            messages.sendSound(view.player, SoundKey.SUCCESS)
                        }

                        false -> { // Failed to increment
                            when (shopType) {
                                ShopType.BUY -> {
                                    messages.sendMiniMessage(view.player, MessageKey.SHOP_INSUFFICIENT_STOCK)
                                }

                                ShopType.SELL -> {
                                    messages.sendMiniMessage(view.player, MessageKey.SHOP_PLAYER_INSUFFICIENT_STOCK)
                                }
                            }
                            messages.sendSound(view.player, SoundKey.FAILURE)
                        }
                    }
                    view.redrawComplete()
                }
            }
            pane[0, 3] = StaticElement(
                drawable(
                    getIncrementCycleButton()
                )
            ) { _ ->
                plugin.async {
                    shopAccessService.cycleMultiplier(view.player.uniqueId)
                    messages.sendMiniMessage(view.player, MessageKey.SHOP_INCREMENT_CYCLE)
                    messages.sendSound(view.player, SoundKey.SUCCESS)
                    view.redrawComplete()
                }
            }
            pane[0, 4] = StaticElement(
                drawable(
                    getIncrementDownButton()
                )
            ) { _ ->
                plugin.async {
                    when (shopAccessService.decrement(playerId)) {
                        true -> { // Successfully decremented
                            messages.sendMiniMessage(view.player, MessageKey.SHOP_INCREMENT_DOWN)
                            messages.sendSound(view.player, SoundKey.SUCCESS)
                        }

                        false -> { // Failed to decrement
                            messages.sendMiniMessage(view.player, MessageKey.SHOP_BELOW_ZERO)
                            messages.sendSound(view.player, SoundKey.FAILURE)
                            return@async
                        }
                    }
                    view.redrawComplete()
                }
            }
        }
    }
}