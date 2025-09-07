package com.brinkmc.plop.shared.gui.shop.access

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// The menu people see when they access a shop which is already created!
class MenuShopMain(override val plugin: Plop): Addon {

    val BAD: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            .name("shop.bad-amount.name")
            .description("shop.bad-amount.desc")

    val SELL: ItemStack
        get() = ItemStack(Material.GOLD_INGOT)
            .name("shop.sell.name")
            .description("shop.sell.desc")

    val BUY: ItemStack
        get() = ItemStack(Material.HOPPER)
            .name("shop.buy.name")
            .description("shop.buy.desc")

    val SETTINGS: ItemStack
        get() = ItemStack(Material.COMPARATOR)
            .name("shop.settings.name")
            .description("shop.settings.desc")

    private fun inventory(player: Player, shop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        // Shop buy
        withTransform { pane, view ->
            pane[2, 5] = if (shop.buyPrice == -1.0f) {
                StaticElement(drawable(
                    BAD.name("shop.no-buy.name").description("shop.no-buy.desc")
                ))
            } else {
                StaticElement(drawable(
                    BUY
                )) { (player) -> plugin.async {
                    plugin.menus.shopBuyMenu.open(player, shop, view)
                } }
            }
        }

        // Shop sell
        withTransform { pane, view ->
            pane[2, 3] = if (shop.sellPrice == -1.0f) {
                StaticElement(drawable(
                    BAD.name("shop.no-sell.name").description("shop.no-sell.desc")
                ))
            } else {
                StaticElement(drawable(
                    SELL
                )) { (player) -> plugin.async {
                    plugin.menus.shopSellMenu.open(player, shop, view)
                } }
            }
        }

        // Admin settings menu
        withTransform { pane, view ->
            if (!shop.owner.isPlayer(player)) {
                return@withTransform
            }

            pane[4, 4] = StaticElement(drawable(
                    SETTINGS
                )) { (player) ->
                plugin.async {
                    plugin.menus.shopSettingsMenu.open(player, shop)
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop) {
        if (!shop.open && !shop.owner.isPlayer(player)) {
            player.sendMiniMessage("shop.not-open")
            return
        }

        inventory(player, shop).open(player)
    }
}