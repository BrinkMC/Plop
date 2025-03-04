package com.brinkmc.plop.shared.gui.shop.creation

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopWare(override val plugin: Plop): Addon {

    val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Pair<ShopType, ItemStack>?>>() // Completable requests

    // Inventory items
    val BUY: ItemStack
        get() = ItemStack(Material.GOLD_INGOT)
            .name("shop.ware.buy.name")
            .description("shop.ware.buy.desc")

    val SELL: ItemStack
        get() = ItemStack(Material.NETHER_WART)
             .name("shop.ware.sell.name")
             .description("shop.ware.sell.desc")

    val CONFIRM: ItemStack
        get()= ItemStack(Material.EMERALD)
            .name("shop.ware.confirm.name")
            .description("shop.ware.confirm.desc")

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)
            .name("shop.ware.back.name")
            .description("shop.ware.back.desc")

    private val inventory = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 1

        val shopWareProperty = interfaceProperty(ItemStack(Material.BARRIER))
        var shopWare by shopWareProperty

        val shopTypeProperty = interfaceProperty(ShopType.SELL)
        var shopType by shopTypeProperty

        withTransform(shopWareProperty, shopTypeProperty) { pane, view ->
            pane[0, 0] = StaticElement(Drawable.Companion.drawable(BACK)) { (player) ->
                plugin.async {
                    view.close()
                }
            }

            pane[0, 3] = when (shopType) {
                ShopType.BUY -> StaticElement(Drawable.Companion.drawable(BUY)) { (player) ->
                    plugin.async {
                        // Set the shop type to buy inside the temporary shop to the other one
                        shopType = ShopType.SELL
                    }
                }

                ShopType.SELL -> StaticElement(Drawable.Companion.drawable(SELL)) { (player) ->
                    plugin.async {
                        shopType = ShopType.BUY
                    }
                }
            }

            pane[0, 5] = StaticElement(
                Drawable.Companion.drawable(
                    shopWare.description("shop.ware.selected")
                )
            )

            pane[0, 8] = StaticElement(Drawable.Companion.drawable(CONFIRM)) { (player) ->
                plugin.async {
                    finalSelection[player]?.complete(Pair(shopType, shopWare))
                    view.close()
                }
            }
        }

        // CLONE PLAYER INVENTORY AND GET CLICK EVENT FOR EACH ONE
        withTransform(shopWareProperty, shopTypeProperty) { pane, view ->
            val cloned = inventoryClone[view.player] ?: return@withTransform

            for (item in cloned) { // Populate the player inventory with their own items
                for (i in 1..4) {
                    for (j in 0..8) {
                        if (item != null) { // Continue
                            pane[i, j] = StaticElement(Drawable.Companion.drawable(item.clone())) { (player) ->
                                plugin.async {
                                    shopWare = item
                                }
                            }
                        }
                    }
                }
            }
        }

        addCloseHandler { reasons, handler ->
            if (finalSelection[handler.player]?.isCompleted == false) {
                finalSelection[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            resetFull(handler.player)

            if (handler.parent() != null) {
                handler.parent()?.open()
            }

            returnInventory(handler.player)
        }
    }

    suspend fun requestChoice(player: Player, parent: InterfaceView? = null): Pair<ShopType, ItemStack>? {
        resetFull(player)
        // Store the request
        val request = CompletableDeferred<Pair<ShopType, ItemStack>?>()
        finalSelection[player] = request
        inventoryClone[player] = player.inventory.contents.clone()
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            resetFull(player)
        }
    }

    suspend fun isActive(player: Player): Boolean {
        return finalSelection[player]?.isCompleted == false
    }

    fun resetFull(player: Player) {
        finalSelection.remove(player)
        returnInventory(player)
        inventoryClone.remove(player)
    }

    fun returnInventory(player: Player) {
        player.inventory.contents = inventoryClone[player] ?: return
    }
}