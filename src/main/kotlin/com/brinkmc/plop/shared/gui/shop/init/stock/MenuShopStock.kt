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

    private fun inventory(player: Player, inputShop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        // Setup different sections of the interface
        setupBackButton(shopProperty)
        setupShopItemsDisplay(shopProperty)
        setupPlayerInventory(shopProperty)

        // Close handler logic
        addCloseHandler { _, handler ->
            // Return inventory to player
            returnInventory(player)

            // Open parent interface if exists
            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupBackButton(shopProperty: InterfaceProperty<Shop>) {
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


    // Shop items
    private fun CombinedInterfaceBuilder.setupShopItemsDisplay(shopProperty: InterfaceProperty<Shop>) {
        withTransform(shopProperty) { pane, view ->
            val shop by shopProperty
            var remainingQuantity = shop.quantity * shop.item.amount

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
                            shop.setQuantity(shop.quantity - (itemStack.amount/shop.item.amount))
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
        shopProperty: InterfaceProperty<Shop>
    ) {
        withTransform(shopProperty) { pane, view ->
            val shop by shopProperty
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
                            shop.setQuantity(shop.quantity + (item.amount/shop.item.amount))

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