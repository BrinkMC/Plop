package com.brinkmc.plop.shared.gui.shop.init.stock

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopStock(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>()
    val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()

    val BAD: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            .name("shop.bad-amount.name")
            .description("shop.bad-amount.desc")

    val BACK: ItemStack
    get() = ItemStack(Material.REDSTONE)
        .name("shop.back-stock.name")
        .description("shop.back-stock.desc")

    val NEED_RESTOCK: ItemStack
    get() = ItemStack(Material.REDSTONE_BLOCK)
        .name("shop.need-restock.name")
        .description("shop.need-restock.desc")

    private fun inventory(player: Player, inputShop: Shop, clone: Array<ItemStack?>) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        // Back button
        withTransform(shopProperty) { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BACK
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }

        withTransform(shopProperty) { pane, view ->
            var tempQuantity = shop.quantity
            // Draw every slot in the "chest" with items till full
            while (tempQuantity > 0) {
                for (i in 0..3) {
                    for (j in 0..8) {
                        val amount = if (tempQuantity >= shop.item.maxStackSize) shop.item.maxStackSize else tempQuantity
                        if (amount == 0) break
                        val item = shop.item.clone().apply { this.amount = amount }

                        pane[i, j] = StaticElement(drawable(item)) { (player, view) ->
                            plugin.async { // Add this item to the player inventory
                                val index = inventoryClone[player]?.indexOf(null) ?: run {
                                    player.sendMiniMessage("shop.inv-full")
                                    return@async
                                }
                                inventoryClone[player]?.set(index, item)
                                shop.setQuantity(shop.quantity - item.amount)
                                view.redrawComplete()
                            }
                        }
                        pane[i, j] = StaticElement(drawable(
                            BAD
                        )) { (player) -> plugin.async {
                            // Do nothing
                        } }
                    }
                }
            }
        }


        // Populate with player inventory / main item selection
        withTransform(shopProperty) { pane, view ->
            // Map inventory contents to the correct grid position
            for (index in clone.indices) {
                val item = clone[index] ?: continue

                // Calculate position in the grid
                val row = (index / 9) + 5  // +1 because row 0-4 has shop controls
                val col = index % 9

                // Skip armor slots and offhand (indices 36-40)
                if (index >= 36) continue

                // Ensure we don't go beyond our grid size
                if (row >= 9) continue

                pane[row, col] = StaticElement(drawable(item)) { (player, view) ->
                    plugin.async { // Delete this item from the player inventory
                        pane[row, col] = StaticElement(drawable(Material.AIR)) // Set this slot to air
                        inventoryClone[player]?.set(index, null)
                        shop.setQuantity(shop.quantity + item.amount)
                        view.redrawComplete()
                    }
                }
            }
        }

        addCloseHandler { reasons, handler ->
            if (completion[handler.player]?.isCompleted == false) {
                completion[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            returnInventory(player)

            if (handler.parent() != null) {
                handler.parent()?.open()
            }

            completion.remove(handler.player)
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): Shop? {
        val request = CompletableDeferred<Shop?>()

        inventoryClone[player] = player.inventory.contents.clone()

        completion[player] = request

        inventory(player, shop.getSnapshot(), player.inventory.contents.clone()).open(player, parentView)
        return request.await()
    }

    fun returnInventory(player: Player) {
        player.inventory.contents = inventoryClone[player]
    }
}