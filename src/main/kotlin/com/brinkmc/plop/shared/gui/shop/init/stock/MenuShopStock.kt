package com.brinkmc.plop.shared.gui.shop.init.stock

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>()
    private val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()

    // Base items initialized only once
    private object BaseItems {
        val BACK = ItemStack(Material.REDSTONE)
        val NEED_RESTOCK = ItemStack(Material.REDSTONE_BLOCK)
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

    private fun inventory(player: Player, inputShop: Shop, clone: Array<ItemStack?>) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        // Setup different sections of the interface
        setupBackButton(shopProperty)
        setupShopItemsDisplay(shopProperty)
        setupPlayerInventory(shopProperty, clone)

        // Close handler logic
        addCloseHandler { _, handler ->
            // Handle completion if not already completed
            if (completion[handler.player]?.isCompleted == false) {
                completion[handler.player]?.complete(shop)
            }

            // Return inventory to player
            returnInventory(player)

            // Open parent interface if exists
            if (handler.parent() != null) {
                handler.parent()?.open()
            }

            // Clean up
            completion.remove(handler.player)
        }
    }

    private fun CombinedInterfaceBuilder.setupBackButton(shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>) {
        withTransform(shopProperty) { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                getItem(BaseItems.BACK, "menu.back")
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupShopItemsDisplay(shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>) {
        withTransform(shopProperty) { pane, view ->
            val shop by shopProperty
            var remainingQuantity = shop.quantity

            // Handle empty to begin with
            if (remainingQuantity == 0) {
                pane[2, 4] = StaticElement(drawable(
                    getItem(BaseItems.NEED_RESTOCK, "shop.need-restock")
                ))
                return@withTransform
            }

            // Display stacks in the shop area (rows 0-3)
            for (i in 0..3) {
                for (j in 0..8) {
                    if (remainingQuantity <= 0) {
                        // No more items to display
                        break
                    }

                    // Calculate stack size for this slot
                    val stackSize = minOf(remainingQuantity, shop.item.maxStackSize)
                    remainingQuantity -= stackSize

                    // Create item stack with appropriate amount
                    val itemStack = shop.item.clone().apply { amount = stackSize }

                    pane[i, j] = StaticElement(drawable(itemStack)) { (player) ->
                        plugin.async {
                            // Find empty slot in player inventory
                            val index = inventoryClone[player]?.indexOfFirst { it == null } ?: -1
                            if (index == -1) {
                                player.sendMiniMessage("shop.inv-full")
                                return@async
                            }

                            // Transfer item to player inventory
                            inventoryClone[player]?.set(index, itemStack)
                            shop.setQuantity(shop.quantity - itemStack.amount)
                            view.redrawComplete()
                        }
                    }
                }

                if (remainingQuantity <= 0) {
                    // No more items to display
                    break
                }
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupPlayerInventory(
        shopProperty: com.noxcrew.interfaces.properties.InterfaceProperty<Shop>,
        clone: Array<ItemStack?>
    ) {
        withTransform(shopProperty) { pane, view ->
            val shop by shopProperty

            // Map inventory contents to the grid
            for (index in clone.indices) {
                // Skip empty slots and armor/offhand slots
                val item = clone[index] ?: continue
                if (index >= 36) continue

                // Calculate position in the interface
                val row = (index / 9) + 5
                val col = index % 9

                pane[row, col] = StaticElement(drawable(item)) { (player) ->
                    plugin.async {
                        // Only transfer if item is similar to shop item
                        if (item.isSimilar(shop.item)) {
                            // Update player inventory
                            inventoryClone[player]?.set(index, null)

                            // Update shop quantity
                            shop.setQuantity(shop.quantity + item.amount)

                            // Redraw the interface
                            view.redrawComplete()
                        }
                    }
                }
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): Shop? {
        // Create completable for async result
        val request = CompletableDeferred<Shop?>()
        completion[player] = request

        // Clone player inventory
        inventoryClone[player] = player.inventory.contents.clone()

        // Open the interface and wait for completion
        inventory(player, shop.getSnapshot(), player.inventory.contents.clone()).open(player, parentView)
        return request.await()
    }

    private fun returnInventory(player: Player) {
        inventoryClone[player]?.let {
            player.inventory.contents = it
        }
    }
}