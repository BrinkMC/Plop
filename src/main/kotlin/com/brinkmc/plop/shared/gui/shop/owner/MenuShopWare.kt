package com.brinkmc.plop.shared.gui.shop.owner

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopWare(override val plugin: Plop): Addon {

    private val temporaryType = mutableMapOf<Player, ShopType>()
    private val temporaryWare = mutableMapOf<Player, ItemStack>()
    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Pair<ShopType, ItemStack>?>>() // Completable requests

    // Inventory items
    val BUY: ItemStack = ItemStack(Material.GOLD_INGOT)
        .name("shop.ware.buy.name")
        .description("shop.ware.buy.desc")

    val SELL: ItemStack = ItemStack(Material.NETHER_WART)
        .name("shop.ware.sell.name")
        .description("shop.ware.sell.desc")

    val CONFIRM: ItemStack = ItemStack(Material.EMERALD)
        .name("shop.ware.confirm.name")
        .description("shop.ware.confirm.desc")

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 1

        val shopWareProperty = interfaceProperty(ItemStack(Material.BARRIER))
        var shopWare by shopWareProperty

        val shopTypeProperty = interfaceProperty(ShopType.SELL)
        var shopType by shopTypeProperty

        withTransform(shopWareProperty, shopTypeProperty) { pane, view ->
            pane[0, 3] = when (shopType) {
                ShopType.BUY -> StaticElement(drawable(BUY)) { (player) -> plugin.async {
                    // Set the shop type to buy inside the temporary shop to the other one
                    temporaryType[player] = ShopType.SELL
                } }
                ShopType.SELL -> StaticElement(drawable(SELL)) { (player) -> plugin.async {
                    temporaryType[player] = ShopType.BUY
                } }
            }

            val shopWareCopy = ItemStack(shopWare)

            pane[0, 5] = StaticElement(drawable(shopWare)) { (player) -> plugin.async {
                temporaryWare[player] = shopWare
            } }

            pane[0,8] = StaticElement(drawable(CONFIRM)) { (player) -> plugin.async {
                val type = temporaryType[player] ?: return@async
                val ware = temporaryWare[player] ?: return@async
                finalSelection[player]?.complete(Pair(type, ware))
                view.close()
            } }
        }
        // TODO THIS IS NOT DONE BECAUSE THE LIBRARY ISN'T LIBRARYING
        addPreprocessor { handler  ->
            // Only continue if the clicked item is within the player inventory and not container inventory
            if (handler.slot > 54) {
                return@addPreprocessor
            }
        }

        addCloseHandler { reasons, handler  ->
            if (finalSelection[handler.player]?.isCompleted == false) {
                finalSelection[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    suspend fun requestChoice(player: Player, parent: InterfaceView? = null): Pair<ShopType, ItemStack>? {
        // Store the request
        val request = CompletableDeferred<Pair<ShopType, ItemStack>?>()
        finalSelection[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            finalSelection.remove(player) // Remove the request because it's been fulfilled already
        }
    }
}