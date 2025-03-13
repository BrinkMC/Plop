package com.brinkmc.plop.shared.gui.shop.init

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.message.ItemKey
import com.brinkmc.plop.shared.util.message.MessageKey
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import io.lumine.shadow.Static
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockDataMeta
import java.util.UUID

class MenuShopCreate(override val plugin: Plop): Addon {

    // Creation stages
    enum class ShopStage {
        ITEM_SELECTION,      // Stage 0: Initial item selection
        PRICE_PENDING,   // Stage 1: Item selected, buy price OR sell price needs setup
        BUY_LIMIT_COMPLETE,  // Stage 2: Buy limit set (You MUST HAVE BUY PRICE to do Stage 5)
        BUY_COMPLETE,        // Stage 3: Buy price set
        SELL_COMPLETE,       // Stage 4: Sell price set Stage 3 & 4 are equivalent
        STOCK_COMPLETE       // Stage 5: Stock configured NOT NECESSARY FOR BUY SHOP DO NOT NEED STOCK
    }

    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        // Interface configuration remains unchanged
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        var shop = inputShop

        val stageProperty = interfaceProperty(ShopStage.ITEM_SELECTION)
        var stage by stageProperty

        withTransform(stageProperty) { pane, view ->
            // Determine what stage it is at
            when {
                shop.quantity > 0 -> {
                    stage = ShopStage.STOCK_COMPLETE
                }
                shop.buyPrice > 0.0f -> {
                    stage = ShopStage.BUY_COMPLETE
                }
                shop.sellPrice > 0.0f -> {
                    stage = ShopStage.SELL_COMPLETE
                }
                shop.buyLimit > 0 -> {
                    stage = ShopStage.BUY_LIMIT_COMPLETE
                }
                shop.item.type == Material.AIR -> {
                    stage = ShopStage.ITEM_SELECTION
                }
                shop.buyPrice == -1.0f && shop.sellPrice == -1.0f -> {
                    stage = ShopStage.PRICE_PENDING
                }
            }
            view.title(lang.deserialise(MessageKey.MENU_CREATE_TITLE))
        }

        // Setup UI elements with modified item creation
        setupItemSelection(shop, stageProperty)
        setupBuyOptions(shop, stageProperty)
        setupBuyLimitOptions(shop, stageProperty)
        setupSellOptions(shop, stageProperty)
        setupStockOptions(shop, stageProperty)
        setupConfirmation(shop, stageProperty, player)

        // Close handler remains unchanged
        addCloseHandler { _, handler ->
            if (handler.isTreeOpened) { // Child so ignore
                return@addCloseHandler
            }

            plugin.shops.creationHandler.cancelShopCreation(player)
        }
    }

    private fun ChestInterfaceBuilder.setupItemSelection(shop: Shop, stageProperty: InterfaceProperty<ShopStage>) {
        var stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[0, 4] = StaticElement(drawable(
                ItemKey.CREATE_CHOOSE.get(MessageKey.MENU_CREATE_CHOOSE_ITEM_NAME, MessageKey.MENU_CREATE_CHOOSE_ITEM_DESC)
            )) { (player) ->
                plugin.async {
                    plugin.menus.shopInitItemMenu.open(player, shop, view)
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBuyLimitOptions(shop: Shop, stageProperty: InterfaceProperty<ShopStage>) {
        var stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[2, 2] = when {
                stage == ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_CREATE_NO_ITEM_NAME, MessageKey.MENU_CREATE_NO_ITEM_DESC)
                    ))
                }
                shop.buyPrice == -1.0f -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_CREATE_NO_BUY_SELL_NAME, MessageKey.MENU_CREATE_NO_BUY_SELL_DESC)
                    ))
                }
                !shop.isBuy() -> {
                    StaticElement(drawable(
                        ItemKey.BAD.get(MessageKey.MENU_CREATE_NO_BUY_SELL_NAME, MessageKey.MENU_CREATE_NO_BUY_SELL_DESC)
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        ItemKey.BUY.get(MessageKey.MENU_BUY_LIMIT_NAME, MessageKey.MENU_BUY_LIMIT_DESC)
                    )) { (player) ->
                        plugin.async {
                            plugin.menus.shopInitBuyLimitMenu.open(player, shop, view)
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBuyOptions(shop: Shop, stageProperty: InterfaceProperty<ShopStage>) {
        var stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[2, 3] = when {
                stage == ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                shop.buyPrice == -1.0f -> {
                    StaticElement(drawable(
                        BaseItems.CLICK_ENABLE.get("shop.create.click-enable.name", "shop.create.click-enable.desc")
                    )) { (player) ->
                        plugin.async {
                            shop.setBuyPrice(0.0f)
                            view.redrawComplete()
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        BaseItems.BUY.get("shop.create.buy.name", "shop.create.buy.desc")
                    )) { (player, view, click) ->
                        plugin.async {
                            if (click.isRightClick) {
                                plugin.menus.shopInitBuyLimitMenu.open(player, shop, view)
                            }
                            else if (click.isLeftClick) {
                                plugin.menus.shopInitBuyMenu.open(player, shop, view)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupSellOptions(shop: Shop, stageProperty: InterfaceProperty<ShopStage>) {
        var stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[2, 5] = when {
                stage == ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                shop.sellPrice == -1.0f -> {
                    StaticElement(drawable(
                        BaseItems.CLICK_ENABLE.get("shop.create.click-enable.name", "shop.create.click-enable.desc")
                    )) { (player) ->
                        plugin.async {
                            shop.setSellPrice(0.0f)
                            view.redrawComplete()
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        BaseItems.SELL.get("shop.create.sell.name", "shop.create.sell.desc")
                    )) { (player, view, click) ->
                        plugin.async {
                            plugin.menus.shopInitSellMenu.open(player, shop, view)
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupStockOptions(shop: Shop, stageProperty: InterfaceProperty<ShopStage>) {
        val stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[4, 4] = when (stage) {
                ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                ShopStage.PRICE_PENDING, ShopStage.BUY_LIMIT_COMPLETE -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.choose-buy-sell.name", "shop.create.choose-buy-sell.desc")
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        BaseItems.STOCK.get("shop.create.stock.name", "shop.create.stock.desc")
                    )) { (player) ->
                        plugin.async {
                            plugin.menus.shopInitStockMenu.open(player, shop, view)
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupConfirmation(shop: Shop, stageProperty: InterfaceProperty<ShopStage>, player: Player) {
        val stage by stageProperty
        withTransform(stageProperty) { pane, view ->
            pane[2, 7] = when (stage) {
                ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                ShopStage.PRICE_PENDING, ShopStage.BUY_LIMIT_COMPLETE -> {
                    StaticElement(drawable(
                        BaseItems.BAD.get("shop.create.choose-buy-sell.name", "shop.create.choose-buy-sell.desc")
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        BaseItems.CONFIRM.get("shop.create.confirm.name", "shop.create.confirm.desc")
                    )) { (player) ->
                        plugin.async {
                            view.close()
                            shops.creationHandler.finaliseShop(player, shop)
                        }
                    }
                }
            }
        }
    }


    suspend fun open(player: Player, shop: Shop): InterfaceView? {
        return inventory(player, shop).open(player)
    }


}