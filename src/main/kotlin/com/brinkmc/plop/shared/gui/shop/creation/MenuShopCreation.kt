package com.brinkmc.plop.shared.gui.shop.creation

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import com.brinkmc.plop.shop.shop.ShopType
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

// Overarching menu for creating a shop
class MenuShopCreation(override val plugin: Plop): Addon {

    private val temporaryShop = mutableMapOf<Player, Location>()

    private val temporaryChoice1 = mutableMapOf<Player, Pair<ShopType, ItemStack>?>()
    private val temporaryChoice2 = mutableMapOf<Player, Pair<Int, Int>?>()
    private val temporaryChoice3 = mutableMapOf<Player, Float?>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Shop?>>() // Completable requests

    // Inventory items
    val STAGE_ONE: ItemStack
        get() = ItemStack(Material.NETHER_WART)
        .name("shop.create.choose.name")
        .description("Shop.create.choose.desc")


    val STAGE_TWO: ItemStack
        get() = ItemStack(Material.BARREL)
        .name("shop.create.stock.name")
        .description("Shop.create.stock.desc")


    val STAGE_THREE: ItemStack
        get() = ItemStack(Material.GOLD_NUGGET)
        .name("shop.create.price.name")
        .description("Shop.create.price.desc")

    val PLEASE_FILL: ItemStack
        get() = ItemStack(Material.BARRIER)
        .name("shop.create.fill.name")
        .description("shop.create.fill.desc")

    val INDICATOR_GOOD: ItemStack
        get() = ItemStack(Material.GREEN_CONCRETE)

    val INDICATOR_BAD: ItemStack
        get() = ItemStack(Material.RED_CONCRETE)

    val CONFIRM: ItemStack
        get() = ItemStack(Material.EMERALD)
    .name("shop.create.confirm.name")
    .description("shop.create.confirm.desc")


    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 2

        val doneProperty = interfaceProperty(false)
        var done by doneProperty

        withTransform { pane, view ->

            // Stage selection parts of the shop creation
            pane[0, 2] = StaticElement(Drawable.Companion.drawable(STAGE_ONE)) { (player) ->
                plugin.async {
                    temporaryChoice1[player] = plugin.menus.shopWareMenu.requestChoice(player, view)
                    view.redrawComplete()
                }
            }

            val choice1 = temporaryChoice1[view.player]
            pane[0, 4] = if (choice1 != null) {
                StaticElement(Drawable.Companion.drawable(STAGE_TWO)) { (player) ->
                    plugin.async {
                        temporaryChoice2[player] = plugin.menus.shopStockMenu.requestChoice(player, choice1, view)
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(Drawable.Companion.drawable(PLEASE_FILL))
            }

            val choice2 = temporaryChoice2[view.player]
            pane[0, 6] = if (choice2 != null && choice1 != null) {
                StaticElement(Drawable.Companion.drawable(STAGE_THREE)) { (player) ->
                    plugin.async {
                        temporaryChoice3[player] =
                            plugin.menus.shopPriceMenu.requestChoice(player, choice1, choice2, view)
                        view.redrawComplete()
                        done = true
                    }
                }
            } else {
                StaticElement(Drawable.Companion.drawable(PLEASE_FILL))
            }
        }

        withTransform { pane, view ->
            // Completion checks to determine concrete colours underneath
            pane[1, 2] = if (temporaryChoice1[view.player] == null) {
                StaticElement(Drawable.Companion.drawable(INDICATOR_BAD))
            } else {
                StaticElement(Drawable.Companion.drawable(INDICATOR_GOOD))
            }

            pane[1, 4] = if (temporaryChoice2[view.player] == null) {
                StaticElement(Drawable.Companion.drawable(INDICATOR_BAD))
            } else {
                StaticElement(Drawable.Companion.drawable(INDICATOR_GOOD))
            }

            pane[1, 6] = if (temporaryChoice3[view.player] == null) {
                StaticElement(Drawable.Companion.drawable(INDICATOR_BAD))
            } else {
                StaticElement(Drawable.Companion.drawable(INDICATOR_GOOD))
            }
        }

        withTransform(doneProperty) { pane, view ->
            // Prove that all 3 stages are done then
            val c1 = temporaryChoice1[view.player]
            val c2 = temporaryChoice2[view.player]
            val c3 = temporaryChoice3[view.player]

            if (c1 != null && c2 != null && c3 != null) {
                val plot = view.player.getCurrentPlot() ?: return@withTransform
                val location = temporaryShop[view.player] ?: return@withTransform

                pane[1, 8] = StaticElement(Drawable.Companion.drawable(CONFIRM)) { (player) ->
                    plugin.async {
                        val shop = Shop( // Create the shop using information given
                            shopId = UUID.randomUUID(),
                            plotId = plot.plotId,
                            plotType = plot.type,
                            _location = location,
                            _shopType = c1.first,
                            _ware = c1.second,
                            _stock = c2.first,
                            _stockLimit = c2.second,
                            _open = true,
                            _price = c3
                        )
                        // Extract resources from player inventory
                        if (c1.first == ShopType.SELL) {
                            removeFromInventory(player, c1.second, c2.first)
                        }
                        finalSelection[player]?.complete(shop)
                        view.close()
                    }
                }
            }
        }

        addCloseHandler { reason, handler ->
            // Check the interface view to see if the player is still in a menu / making a menu?
            if (handler.isTreeOpened) {
                return@addCloseHandler
            }

            // They have actually left the menu
            if (finalSelection[handler.player]?.isCompleted == false) {
                finalSelection[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            resetFull(handler.player) // No more temporary values ready for the next attempt
        }
    }

    suspend fun requestChoice(player: Player, location: Location?, parent: InterfaceView? = null): Shop? {
        resetFull(player) // Reset all temporary values just in case they exist
        if (location == null) { // No location, ergo no shop
            return null
        }

        temporaryShop[player] = location // Set the location of the shop chest

        val request = CompletableDeferred<Shop?>()
        finalSelection[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            resetFull(player)
        }
    }

    fun resetFull(player: Player) { // Reset all temporary values
        temporaryShop.remove(player)
        temporaryChoice1.remove(player)
        temporaryChoice2.remove(player)
        temporaryChoice3.remove(player)
        finalSelection.remove(player)
    }

    suspend fun isChoice(location: Location): Boolean {
        return temporaryShop.values.contains(location)
    }

    fun removeFromInventory(player: Player, itemStack: ItemStack, number: Int) {
        var amount = number
        for (i in 0..player.inventory.contents.size) {
            val bigItem = player.inventory.contents[i]
            if (bigItem == null) {
                continue
            }
            if (bigItem.isSimilar(itemStack)) {
                if (amount < bigItem.amount) {
                    bigItem.amount -= amount
                    return
                } else {
                    amount -= bigItem.amount
                    player.inventory.remove(bigItem)
                }

            }
        }
    }
}