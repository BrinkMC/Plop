package com.brinkmc.plop.shared.gui.shop.init

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import io.lumine.shadow.Static
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockDataMeta
import java.util.UUID

class MenuShopCreate(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>() // Completable requests



    val BAD: ItemStack
        get() = ItemStack(Material.BARRIER)

    val CLICK_ENABLE: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            .name("shop.create.click-enable.name")
            .description("shop.create.click-enable.desc")

    val ITEM_CHOOSE: ItemStack
        get() = ItemStack(Material.VERDANT_FROGLIGHT)
            .name("shop.create.choose.name")
            .description("shop.create.choose.desc")

    val BUY: ItemStack
        get() = ItemStack(Material.HOPPER)
            .name("shop.create.buy.name")
            .description("shop.create.buy.desc")

    val SELL: ItemStack
        get() = ItemStack(Material.CHEST)
            .name("shop.create.sell.name")
            .description("shop.create.sell.desc")

    val STOCK: ItemStack
        get() = ItemStack(Material.BARREL)
            .name("shop.create.stock.name")
            .description("shop.create.stock.desc")

    val CONFIRM: ItemStack
        get() = ItemStack(Material.EMERALD)
            .name("shop.create.confirm.name")
            .description("shop.create.confirm.desc")

    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        val stageProperty = interfaceProperty(0)
        var stage by stageProperty

        // Main item selection step
        withTransform(shopProperty, stageProperty) { pane, view ->
            pane[0, 4] = StaticElement(drawable(
                ITEM_CHOOSE
            )) { (player) ->
                plugin.async { // Open shop item menu
                    stage = 0
                    shop = plugin.menus.shopInitItemMenu.open(player, shop, view) ?: return@async
                    stage = 1
                }
            }
        }

        // Buy selection
        withTransform(shopProperty, stageProperty) { pane, view ->
            // Check if shop item is barrier
            pane[2, 3] = if ((shop.item.type == Material.AIR)) {
                StaticElement(drawable(
                    BAD.name("shop.create.fill-item.name").description("shop.create.fill-item.desc")
                ))
            } else if (shop.buyPrice == -1.0f) { // Enable buy shop, otherwise disabled
                StaticElement(drawable(
                    CLICK_ENABLE
                )) { (player) ->
                    plugin.async {
                        shop.setBuyPrice(0.0f)
                    }
                }
            } else { // Enabled thus please set / modify
                StaticElement(drawable(
                    BUY
                )) { (player, view, click) ->
                    plugin.async {
                        if (click.isRightClick) {
                            stage = if (stage == 1) 1 else 3
                            shop = plugin.menus.shopInitBuyLimitMenu.open(player, shop, view) ?: return@async
                            stage = if (stage == 1) 2 else 3
                        }
                        else if (click.isLeftClick) {
                            stage = 1
                            shop = plugin.menus.shopInitBuyMenu.open(player, shop, view) ?: return@async
                            stage = 3

                        }
                    }
                }
            }
        }

        // Sell selection
        withTransform(shopProperty, stageProperty) { pane, view ->
            // Check if shop item is barrier
            pane[2, 5] = if ((shop.item.type == Material.AIR)) {
                StaticElement(drawable(
                    BAD.name("shop.create.fill-item.name").description("shop.create.fill-item.desc")
                ))
            } else if (shop.sellPrice == -1.0f) { // Enable buy shop, otherwise disabled
                StaticElement(drawable(
                    CLICK_ENABLE
                )) { (player) ->
                    plugin.async {
                        shop.setSellPrice(0.0f)
                        view.redrawComplete()
                    }
                }
            } else { // Enabled thus please set / modify
                StaticElement(drawable(
                    SELL
                )) { (player, view, click) ->
                    plugin.async {
                        stage = 1
                        shop = plugin.menus.shopInitSellMenu.open(player, shop, view) ?: return@async
                        stage = 4
                    }
                }
            }
        }

        // Stock selection
        withTransform(shopProperty, stageProperty) { pane, view ->
            pane[4, 4] = if (stage == 0) {
                StaticElement(drawable(
                    BAD.name("shop.create.fill-item.name").description("shop.create.fill-item.desc")
                ))
            } else if (stage == 1 || stage == 2) {
                StaticElement(drawable(
                    BAD.name("shop.create.choose-buy-sell.name").description("shop.create.choose-buy-sell.desc")
                ))
            } else {
                StaticElement(drawable(
                    STOCK
                )) { (player) ->
                    plugin.async {
                        stage = if (stage == 3 || stage == 4) stage else 1
                        shop = plugin.menus.shopInitStockMenu.open(player, shop, view) ?: return@async
                        stage = 5
                    }
                }
            }
        }

        // Confirm button
        withTransform(shopProperty, stageProperty) { pane, view ->
            pane[2, 7] = if (stage == 0) {
                StaticElement(drawable(
                    BAD.name("shop.create.fill-item.name").description("shop.create.fill-item.desc")
                ))
            } else if (stage == 1 || stage == 2) {
                StaticElement(drawable(
                    BAD.name("shop.create.choose-buy-sell.name").description("shop.create.choose-buy-sell.desc")
                ))
            } else {
                StaticElement(drawable(
                    CONFIRM
                )) { (player) ->
                    plugin.async {
                        completion[player]?.complete(shop)
                        view.close()
                    }
                }
            }
        }

        addCloseHandler { reason, handler ->
            // Check the interface view to see if the player is still in a menu / making a menu?
            if (handler.isTreeOpened) {
                return@addCloseHandler
            }

            // They have actually left the menu
            if (completion[handler.player]?.isCompleted == false) {
                completion[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            completion.remove(handler.player)
        }

    }

    suspend fun open(player: Player, chest: Chest, plotId: UUID, plotType: PlotType): Shop? {
        val shop = shops.handler.emptyShop(plotId, plotType)
        shop.setLocation(chest.location)
        shop


        val request = CompletableDeferred<Shop?>()
        completion[player] = request

        inventory(player, shop).open(player)
        return request.await()
    }


}