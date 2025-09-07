package com.brinkmc.plop.shared.gui.shop.init.item

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.dto.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.CombinedInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.text.get

class MenuShopItem(override val plugin: Plop): Addon {

    // Store player inventory clones to restore when menu closes
    val inventoryClone = mutableMapOf<Player, Array<ItemStack?>>()


    // Base items initialized only once
    private object BaseItems {
        val BACK = ItemStack(Material.REDSTONE)
        val CONFIRM = ItemStack(Material.EMERALD)
        val MORE = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        val LESS = ItemStack(Material.RED_STAINED_GLASS_PANE)
        val BAD = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
    }

    private fun inventory(player: Player, inputShop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 5

        val shop = inputShop

        // Setup shared state
        val tempItemProperty = interfaceProperty(inputShop.item.clone())
        val maxAmountProperty = interfaceProperty(inputShop.item.clone().maxStackSize)

        // Setup different sections of the interface
        setupCenterItem(tempItemProperty)
        setupMoreButton(tempItemProperty, maxAmountProperty)
        setupLessButton(tempItemProperty)
        setupConfirmButton(shop, tempItemProperty)
        setupBackButton()
        setupPlayerInventory(tempItemProperty, maxAmountProperty)
        setupCloseHandler(player)
    }

    private fun CombinedInterfaceBuilder.setupCenterItem(tempItemProperty: InterfaceProperty<ItemStack>) {
        withTransform(tempItemProperty) { pane, view ->
            val tempItem by tempItemProperty
            pane[2, 4] = StaticElement(drawable(tempItem))
        }
    }

    private fun CombinedInterfaceBuilder.setupMoreButton(
        tempItemProperty: InterfaceProperty<ItemStack>,
        maxAmountProperty: InterfaceProperty<Int>
    ) {
        var tempItem by tempItemProperty
        var maxAmount by maxAmountProperty
        withTransform(tempItemProperty, maxAmountProperty) { pane, view ->
            pane[2, 6] = if (tempItem.amount < maxAmount) {
                StaticElement(drawable(
                    BaseItems.MORE.get("shop.more-amount.name", "shop.more-amount.desc")
                )) { (player) -> plugin.async {
                    val updatedItem = tempItem.clone()
                    updatedItem.amount += 1
                    tempItem = updatedItem
                    view.redrawComplete()
                }}
            } else {
                StaticElement(drawable(
                    BaseItems.BAD.get("shop.bad-amount.toomuch.name", "shop.bad-amount.toomuch.desc")
                ))
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupLessButton(tempItemProperty: InterfaceProperty<ItemStack>) {
        var tempItem by tempItemProperty
        withTransform(tempItemProperty) { pane, view ->
            pane[2, 2] = if (0 < tempItem.amount) {
                StaticElement(drawable(
                    BaseItems.LESS.get("shop.less-amount.name", "shop.less-amount.desc")
                )) { (player) -> plugin.async {
                    val updatedItem = tempItem.clone()
                    updatedItem.amount -= 1
                    tempItem = updatedItem
                    view.redrawComplete()
                }}
            } else {
                StaticElement(drawable(
                    BaseItems.BAD.get("shop.bad-amount.toolittle.name", "shop.bad-amount.toolittle.desc")
                ))
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupConfirmButton(
        shop: Shop,
        tempItemProperty: InterfaceProperty<ItemStack>
    ) {
        withTransform(tempItemProperty) { pane, view ->
            val tempItem by tempItemProperty

            pane[2, 8] = if (tempItem.type == Material.AIR) {
                StaticElement(drawable(
                    BaseItems.BAD.get("shop.bad-amount.noitem.name", "shop.bad-amount.noitem.desc")
                ))
            } else {
                StaticElement(drawable(
                    BaseItems.CONFIRM.get("shop.confirm-stock.name", "shop.confirm-stock.desc")
                )) { (player) -> plugin.async {
                    shop.setItem(tempItem.clone())
                    view.close()
                }}
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupBackButton() {
        withTransform { pane, view ->
            pane[2, 0] = StaticElement(drawable(
                BaseItems.BACK.get("shop.back-stock.name", "shop.back-stock.desc")
            )) { (player) -> plugin.async {
                view.close()
            }}
        }
    }

    private fun CombinedInterfaceBuilder.setupPlayerInventory(
        tempItemProperty: InterfaceProperty<ItemStack>,
        maxAmountProperty: InterfaceProperty<Int>
    ) {
        var tempItem by tempItemProperty
        var maxAmount by maxAmountProperty


        withTransform { pane, view ->
            var clone = inventoryClone[view.player] ?: return@withTransform
            // Map inventory contents to the correct grid position
            for (index in clone.indices) {
                val item = clone[index] ?: continue

                // Calculate position in the grid
                val row = (index / 9) + 5  // +5 rows for shop controls
                val col = index % 9

                // Skip armor slots and offhand (indices 36-40)
                if (index >= 36) continue

                // Ensure we don't go beyond our grid size
                if (row >= 9) continue

                pane[row, col] = StaticElement(drawable(item)) { (player) -> plugin.async {
                    tempItem = item.clone()
                    maxAmount = item.amount
                    view.redrawComplete()
                }}
            }
        }
    }

    private fun CombinedInterfaceBuilder.setupCloseHandler(player: Player) {
        addCloseHandler { _, handler ->
            returnInventory(player)

            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): InterfaceView {
        // Store the player's inventory to restore later
        inventoryClone[player] = player.inventory.contents.clone()

        return inventory(player, shop).open(player, parentView)
    }

    internal fun returnInventory(player: Player) {
        inventoryClone[player]?.let {
            player.inventory.contents = it
            inventoryClone.remove(player)
        }
    }
}