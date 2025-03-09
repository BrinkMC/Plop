package com.brinkmc.plop.shared.gui.shop.init.stock

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()

    private fun inventory(player: Player, inputShop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        var shop = inputShop

        // Setup different sections of the interface
        setupBackButton(shop)
        setupShopItemsDisplay(shop)
        setupPlayerInventory(shop)

        // Close handler logic
        addCloseHandler { _, handler ->
            // Return inventory to player
            returnInventory(player)

            // Open parent interface if exists
            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupBackButton(shop: Shop) {
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


    // Shop items
    private fun CombinedInterfaceBuilder.setupShopItemsDisplay(shop: Shop) {
        withTransform { pane, view ->
            var remainingQuantity = shop.quantity

            // Handle empty to begin with
            if (remainingQuantity == 0) {
                pane[2, 4] = StaticElement(drawable(
                    BaseItems.NEED_RESTOCK.get("shop.need-restock.name", "shop.need-restock.desc")
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

    // Player items
    private fun CombinedInterfaceBuilder.setupPlayerInventory(
        shop: Shop
    ) {
        withTransform { pane, view ->
            val clone = inventoryClone[view.player] ?: return@withTransform

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
                            pane[row, col] = StaticElement(drawable(Material.AIR)) // Replace item with air

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

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): InterfaceView {
        // Clone player inventory
        inventoryClone[player] = player.inventory.contents.clone()

        // Open the interface and wait for completion
        return inventory(player, shop).open(player, parentView)
    }

    internal fun returnInventory(player: Player) {
        inventoryClone[player]?.let {
            player.inventory.contents = it
        }
    }
}