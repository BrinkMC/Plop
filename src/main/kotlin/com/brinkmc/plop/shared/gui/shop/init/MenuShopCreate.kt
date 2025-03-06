package com.brinkmc.plop.shared.gui.shop.init

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import io.lumine.shadow.Static
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockDataMeta
import java.util.UUID

class MenuShopCreate(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>() // Completable requests

    // Base items initialized only once
    private object BaseItems {
        val BAD = ItemStack(Material.BARRIER)
        val CLICK_ENABLE = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val ITEM_CHOOSE = ItemStack(Material.VERDANT_FROGLIGHT)
        val BUY = ItemStack(Material.HOPPER)
        val SELL = ItemStack(Material.CHEST)
        val STOCK = ItemStack(Material.BARREL)
        val CONFIRM = ItemStack(Material.EMERALD)
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

    // Creation stages
    enum class ShopStage {
        ITEM_SELECTION,      // Stage 0: Initial item selection
        PRICE_PENDING,   // Stage 1: Item selected, buy price OR sell price needs setup
        BUY_LIMIT_COMPLETE,  // Stage 2: Buy limit set (You MUST HAVE BUY PRICE to do Stage 5)
        BUY_COMPLETE,        // Stage 3: Buy price set
        SELL_COMPLETE,       // Stage 4: Sell price set Stage 3 & 4 are equivalent
        STOCK_COMPLETE       // Stage 5: Stock configured
    }


    private fun inventory(player: Player, inputShop: Shop) = buildChestInterface {
        // Interface configuration remains unchanged
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        val stageProperty = interfaceProperty(ShopStage.ITEM_SELECTION)
        var stage by stageProperty

        // Setup UI elements with modified item creation
        setupItemSelection(shopProperty, stageProperty)
        setupBuyOptions(shopProperty, stageProperty)
        setupSellOptions(shopProperty, stageProperty)
        setupStockOptions(shopProperty, stageProperty)
        setupConfirmation(shopProperty, stageProperty, player)

        // Close handler remains unchanged
        addCloseHandler { _, handler ->
            if (!handler.isTreeOpened && completion[handler.player]?.isCompleted == false) {
                completion[handler.player]?.complete(null)
            }
            completion.remove(handler.player)
        }
    }

    private fun ChestInterfaceBuilder.setupItemSelection(shopProperty: InterfaceProperty<Shop>, stageProperty: InterfaceProperty<ShopStage>) {
        withTransform(shopProperty, stageProperty) { pane, view ->
            pane[0, 4] = StaticElement(drawable(
                getItem(BaseItems.ITEM_CHOOSE, "shop.create.choose.name", "shop.create.choose.desc")
            )) { (player) ->
                plugin.async {
                    var stage by stageProperty
                    var shop by shopProperty

                    stage = ShopStage.ITEM_SELECTION
                    shop = plugin.menus.shopInitItemMenu.open(player, shop, view) ?: return@async
                    stage = ShopStage.PRICE_PENDING
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupBuyOptions(shopProperty: InterfaceProperty<Shop>, stageProperty: InterfaceProperty<ShopStage>) {
        withTransform(shopProperty, stageProperty) { pane, view ->
            var shop by shopProperty
            var stage by stageProperty

            pane[2, 3] = when {
                stage == ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                shop.buyPrice == -1.0f -> {
                    StaticElement(drawable(
                        getItem(BaseItems.CLICK_ENABLE, "shop.create.click-enable.name", "shop.create.click-enable.desc")
                    )) { (player) ->
                        plugin.async {
                            shop.setBuyPrice(0.0f)
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BUY, "shop.create.buy.name", "shop.create.buy.desc")
                    )) { (player, view, click) ->
                        plugin.async {
                            if (click.isRightClick) {
                                stage = if (stage == ShopStage.PRICE_PENDING)
                                    ShopStage.PRICE_PENDING else ShopStage.BUY_COMPLETE

                                shop = plugin.menus.shopInitBuyLimitMenu.open(player, shop, view) ?: return@async

                                stage = if (stage == ShopStage.PRICE_PENDING)
                                    ShopStage.BUY_LIMIT_COMPLETE else ShopStage.BUY_COMPLETE
                            }
                            else if (click.isLeftClick) {
                                stage = ShopStage.PRICE_PENDING
                                shop = plugin.menus.shopInitBuyMenu.open(player, shop, view) ?: return@async
                                stage = ShopStage.BUY_COMPLETE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupSellOptions(shopProperty: InterfaceProperty<Shop>, stageProperty: InterfaceProperty<ShopStage>) {
        withTransform(shopProperty, stageProperty) { pane, view ->
            var shop by shopProperty
            var stage by stageProperty

            pane[2, 5] = when {
                stage == ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                shop.sellPrice == -1.0f -> {
                    StaticElement(drawable(
                        getItem(BaseItems.CLICK_ENABLE, "shop.create.click-enable.name", "shop.create.click-enable.desc")
                    )) { (player) ->
                        plugin.async {
                            shop.setSellPrice(0.0f)
                            view.redrawComplete()
                        }
                    }
                }
                else -> {
                    StaticElement(drawable(
                        getItem(BaseItems.SELL, "shop.create.sell.name", "shop.create.sell.desc")
                    )) { (player, view, click) ->
                        plugin.async {
                            stage = ShopStage.PRICE_PENDING
                            shop = plugin.menus.shopInitSellMenu.open(player, shop, view) ?: return@async
                            stage = ShopStage.SELL_COMPLETE
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupStockOptions(shopProperty: InterfaceProperty<Shop>, stageProperty: InterfaceProperty<ShopStage>) {
        withTransform(shopProperty, stageProperty) { pane, view ->
            val stage by stageProperty

            pane[4, 4] = when (stage) {
                ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                ShopStage.PRICE_PENDING, ShopStage.BUY_LIMIT_COMPLETE -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.choose-buy-sell.name", "shop.create.choose-buy-sell.desc")
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        getItem(BaseItems.STOCK, "shop.create.stock.name", "shop.create.stock.desc")
                    )) { (player) ->
                        plugin.async {
                            var stage by stageProperty
                            var shop by shopProperty

                            stage = if (stage == ShopStage.BUY_COMPLETE || stage == ShopStage.SELL_COMPLETE)
                                stage else ShopStage.PRICE_PENDING

                            shop = plugin.menus.shopInitStockMenu.open(player, shop, view) ?: return@async
                            stage = ShopStage.STOCK_COMPLETE
                        }
                    }
                }
            }
        }
    }

    private fun ChestInterfaceBuilder.setupConfirmation(shopProperty: InterfaceProperty<Shop>, stageProperty: InterfaceProperty<ShopStage>, player: Player) {
        withTransform(shopProperty, stageProperty) { pane, view ->
            val stage by stageProperty
            val shop by shopProperty

            pane[2, 7] = when (stage) {
                ShopStage.ITEM_SELECTION -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.fill-item.name", "shop.create.fill-item.desc")
                    ))
                }
                ShopStage.PRICE_PENDING, ShopStage.BUY_LIMIT_COMPLETE -> {
                    StaticElement(drawable(
                        getItem(BaseItems.BAD, "shop.create.choose-buy-sell.name", "shop.create.choose-buy-sell.desc")
                    ))
                }
                else -> {
                    StaticElement(drawable(
                        getItem(BaseItems.CONFIRM, "shop.create.confirm.name", "shop.create.confirm.desc")
                    )) { (player) ->
                        plugin.async {
                            completion[player]?.complete(shop)
                            view.close()
                        }
                    }
                }
            }
        }
    }


    suspend fun open(player: Player, chest: Chest, plotId: UUID, plotType: PlotType): Shop? {
        val shop = shops.handler.emptyShop(plotId, plotType)
        shop.setLocation(chest.location)

        val request = CompletableDeferred<Shop?>()
        completion[player] = request

        inventory(player, shop.getSnapshot()).open(player)
        return request.await()
    }


}