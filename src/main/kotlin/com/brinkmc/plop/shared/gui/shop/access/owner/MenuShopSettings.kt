package com.brinkmc.plop.shared.gui.shop.access.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopSettings(override val plugin: Plop): Addon {


    private object BaseItems {
        val HANDLE_BUY = ItemStack(Material.HOPPER)
        val HANDLE_SELL = ItemStack(Material.GOLD_INGOT)
        val CLICK_ENABLE = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val STOCK = ItemStack(Material.BARREL)
        val TRANSACTION_LOG = ItemStack(Material.PAPER)
        val CLOSE_SHOP = ItemStack(Material.IRON_DOOR)
        val OPEN_SHOP = ItemStack(Material.OAK_DOOR)
        val DELETE_SHOP = ItemStack(Material.BARRIER)
        val BACK = ItemStack(Material.REDSTONE)
        val BAD = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    private fun inventory(player: Player, shop: Shop) = buildChestInterface {
        // Internal settings menu for shop, only to owners
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        // Shop stock button
        shopStock(shop)
        shopTransaction(shop)
        shopToggle(shop)
        deleteShop(shop)
        buyShop(shop)
        sellShop(shop)
        backButton()


        addCloseHandler { _, handler ->
            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    private fun ChestInterfaceBuilder.backButton() {
        // Back button
        withTransform { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BaseItems.BACK.get("menu.back.name", "menu.back.desc")
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.buyShop(shop: Shop) {
        // Shop buy handle
        withTransform { pane, view ->
            if (shop.buyPrice == -1.0f) {
                pane[1, 2] = StaticElement(drawable(
                    BaseItems.CLICK_ENABLE.get("shop.click-enable.name", "shop.click-enable.desc")
                )) { (player, view, click) -> plugin.async {
                    shop.setBuyPrice(0.0f)
                    view.redrawComplete()
                } }
                return@withTransform
            }

            pane[1, 2] = StaticElement(drawable(
                BaseItems.HANDLE_BUY.get("shop.handle-buy.name", "shop.handle-buy.desc")
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isRightClick) {
                        plugin.menus.shopInitBuyLimitMenu.open(player, shop, view)
                    }
                    else if (click.isLeftClick) {
                        plugin.menus.shopInitBuyMenu.open(player, shop, view)
                    }
                    else if (click.isDrop()) { // Disable buy
                        shop.setBuyPrice(-1.0f)
                        view.redrawComplete()
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.sellShop(shop: Shop) {
        // Shop sell handle
        withTransform { pane, view ->
            if (shop.sellPrice == -1.0f) {
                pane[1, 6] = StaticElement(drawable(
                    BaseItems.CLICK_ENABLE.get("shop.click-enable.name", "shop.click-enable.desc")
                )) { (player, view, click) -> plugin.async {
                    shop.setSellPrice(0.0f)
                    view.redrawComplete()
                } }
                return@withTransform
            }

            pane[1, 6] = StaticElement(drawable(
                BaseItems.HANDLE_SELL.get("shop.handle-sell.name", "shop.handle-sell.desc")
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isLeftClick) {
                        plugin.menus.shopInitSellMenu.open(player, shop, view)
                    }
                    else if (click.isDrop()) { // Disable sell
                        shop.setSellPrice(-1.0f)
                        view.redrawComplete()
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.shopStock(shop: Shop) {
        // Shop stock
        withTransform { pane, view ->
            pane[2, 4] = StaticElement(drawable(
                BaseItems.STOCK.get("shop.stock.name", "shop.stock.desc")
            )) { (player) ->
                plugin.async {
                    plugin.menus.shopInitStockMenu.open(player, shop, view)
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.shopTransaction(shop: Shop) {
        // Shop transaction log
        withTransform { pane, view ->
            pane[3, 7] = StaticElement(drawable(
                BaseItems.TRANSACTION_LOG.get("shop.transaction-log.name", "shop.transaction-log.desc")
            )) { (player) ->
                plugin.async {
                    plugin.menus.shopLogsMenu.open(player, shop, view)
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.shopToggle(shop: Shop) {
        withTransform {pane, view ->
            pane[3, 1] = if (shop.open) {
                StaticElement(drawable(
                    BaseItems.OPEN_SHOP.get("shop.open-shop.name", "shop.open-shop.desc")
                )) { (player) ->
                    plugin.async {
                        shop.setOpen(false)
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    BaseItems.CLOSE_SHOP.get("shop.close-shop.name", "shop.close-shop.desc")
                )) { (player) ->
                    plugin.async {
                        shop.setOpen(true)
                        view.redrawComplete()
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.deleteShop(shop: Shop) {
        withTransform { pane, view ->

            if (shop.quantity != 0) {
                pane[4, 0] = StaticElement(drawable(
                    BaseItems.BAD.get("shop.delete-shop.notempty.name", "shop.delete-shop.notempty.desc")
                ))
                return@withTransform
            }

            pane[4, 0] = StaticElement(drawable(
                BaseItems.DELETE_SHOP.get("shop.delete-shop.name", "shop.delete-shop.desc")
            )) { (player) ->
                plugin.async {
                    shops.handler.deleteShop(shop)
                    view.close()
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parent: InterfaceView? = null) {
        inventory(player, shop).open(player, parent)
    }

}