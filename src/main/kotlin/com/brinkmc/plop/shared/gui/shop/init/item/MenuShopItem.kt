package com.brinkmc.plop.shared.gui.shop.init.item

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.get
import kotlin.text.get
import kotlin.text.set

class MenuShopItem(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>()
    val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)
            .name("shop.back-stock.name")
            .description("shop.back-stock.desc")

    val CONFIRM: ItemStack
        get() = ItemStack(Material.EMERALD)
            .name("shop.confirm-stock.name")
            .description("shop.confirm-stock.desc")

    val MORE: ItemStack
        get() = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        .name("shop.more-amount.name")
        .description("shop.more-amount.desc")

    val LESS: ItemStack
        get() = ItemStack(Material.RED_STAINED_GLASS_PANE)
        .name("shop.less-amount.name")
        .description("shop.less-amount.desc")

    val BAD: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        .name("shop.bad-amount.name")
        .description("shop.bad-amount.desc")

    private fun inventory(player: Player, inputShop: Shop, clone: Array<ItemStack?>) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        var tempItem = shop.item.clone()
        var maxAmount = tempItem.amount

        // Centre piece item
        withTransform(shopProperty) { pane, view ->
            pane[2, 4] = StaticElement(drawable(
                tempItem
            ))
        }

        // More button
        withTransform(shopProperty) { pane, view ->
            pane[2, 6] = if (tempItem.amount < maxAmount) {
                StaticElement(drawable(
                    MORE
                )) { (player) ->
                    plugin.async {
                        tempItem.amount += 1
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    BAD.name("shop.bad-amount.toomuch.name").description("shop.bad-amount.toomuch.desc")
                )) // Do nothing
            }
        }

        // Less button
        withTransform(shopProperty) { pane, view ->
            pane[2, 2] = if (0 < tempItem.amount) {
                StaticElement(drawable(
                    LESS
                )) { (player) ->
                    plugin.async {
                        tempItem.amount -= 1
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    BAD.name("shop.bad-amount.toolittle.name").description("shop.bad-amount.toolittle.desc")
                )) // Do nothing
            }
        }


        // Confirm button
        withTransform(shopProperty) { pane, view ->
            pane[2, 8] = if (tempItem.type == Material.AIR) {
                StaticElement(drawable(
                    BAD.name("shop.bad-amount.noitem.name").description("shop.bad-amount.noitem.desc")
                )) // Do nothing
            } else {
                StaticElement(drawable(
                    CONFIRM
                )) { (player) ->
                    plugin.async {
                        shop.setItem(tempItem.clone())
                        completion[player]?.complete(shop)
                        view.close()
                    }
                }
            }
        }

        // Back button
        withTransform(shopProperty) { pane, view ->
            pane[2, 0] = StaticElement(drawable(
                BACK
            )) { (player) ->
                plugin.async {
                    view.close()
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

                pane[row, col] = StaticElement(drawable(item)) { (player) ->
                    plugin.async {
                        tempItem = item.clone() // Clone only when setting shopWare
                        maxAmount = item.amount
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