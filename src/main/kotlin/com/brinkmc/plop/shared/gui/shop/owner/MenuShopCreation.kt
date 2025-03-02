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
import io.lumine.shadow.Static
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.collections.get
import kotlin.collections.set


// Overarching menu for creating a shop
class MenuShopCreation(override val plugin: Plop): Addon {

    private val temporaryShop = mutableMapOf<Player, Location>()

    private val temporaryChoice1 = mutableMapOf<Player, Pair<ShopType, ItemStack>?>()
    private val temporaryChoice2 = mutableMapOf<Player, Pair<Int, Int?>?>()
    private val temporaryChoice3 = mutableMapOf<Player, Float?>()

    private val finalSelection = mutableMapOf<Player, CompletableDeferred<Shop?>>() // Completable requests

    // Inventory items
    val STAGE_ONE: ItemStack = ItemStack(Material.NETHER_WART)
        .name("shop.create.choose.name")
        .description("Shop.create.choose.desc")


    val STAGE_TWO: ItemStack = ItemStack(Material.BARREL)
        .name("shop.create.stock.name")
        .description("Shop.create.stock.desc")


    val STAGE_THREE: ItemStack = ItemStack(Material.GOLD_NUGGET)
        .name("shop.create.price.name")
        .description("Shop.create.price.desc")

    val INDICATOR_GOOD: ItemStack = ItemStack(Material.GREEN_CONCRETE)

    val INDICATOR_BAD: ItemStack = ItemStack(Material.RED_CONCRETE)

    val CONFIRM: ItemStack = ItemStack(Material.EMERALD)
    .name("shop.create.confirm.name")
    .description("shop.create.confirm.desc")


    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 3


        withTransform(stockProperty) { pane, view ->
            val shop = temporaryShop[view.player] ?: return@withTransform
            stock = shop.stock
        }

        withTransform { pane, view ->

            // Stage selection parts of the shop creation
            pane[1, 3] = StaticElement(drawable(STAGE_ONE)) { (player) -> plugin.async {
                temporaryChoice1[player] = plugin.menus.shopWareMenu.requestChoice(player, view)
            } }

            pane[1, 5] = StaticElement(drawable(STAGE_TWO)) { (player) -> plugin.async {
                temporaryChoice2[player] = plugin.menus.shopStockMenu.requestChoice(player, view)
            } }

            pane[1, 7] = StaticElement(drawable(STAGE_THREE)) { (player) -> plugin.async {
                temporaryChoice3[player] = plugin.menus.shopPriceMenu.requestChoice(player, view)
            } }
        }

        withTransform { pane, view ->
            // Completion checks to determine concrete colours underneath
            pane[2, 3] = if (temporaryChoice1[view.player] == null) {
                StaticElement(drawable(INDICATOR_BAD))
            } else {
                StaticElement(drawable(INDICATOR_GOOD))
            }

            pane[2, 5] = if (temporaryChoice2[view.player] == null) {
                StaticElement(drawable(INDICATOR_BAD))
            } else {
                StaticElement(drawable(INDICATOR_GOOD))
            }

            pane[2, 7] = if (temporaryChoice3[view.player] == null) {
                StaticElement(drawable(INDICATOR_BAD))
            } else {
                StaticElement(drawable(INDICATOR_GOOD))
            }
        }

        withTransform { pane, view ->
            // Prove that all 3 stages are done then
            val c1 = temporaryChoice1[view.player]
            val c2 = temporaryChoice2[view.player]
            val c3 = temporaryChoice3[view.player]

            if (c1 != null && c2 != null && c3 != null) {
                val plot = view.player.getCurrentPlot() ?: return@withTransform
                val location = temporaryShop[view.player] ?: return@withTransform

                pane[2, 8] = StaticElement(drawable(CONFIRM)) { (player) -> plugin.async {
                    val shop = Shop( // Create the shop using information given
                        shopId = UUID.randomUUID(),
                        plotId = plot.plotId,
                        plotType = plot.type,
                        location = location,
                        shopType = c1.first,
                        ware = c1.second,
                        stock = c2.first,
                        stockLimit = c2.second ?: 0,
                        open = true,
                        price = c3
                    )

                    temporaryShop.remove(player) // Remove the temporary shop, no need any more
                    finalSelection[player]?.complete(shop)
                    view.close()
                } }
            }
        }

        addCloseHandler { reasons, handler  ->
            if (plotTypeChoice[handler.player]?.isCompleted == false) {
                plotTypeChoice[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            if (handler.parent() != null) {
                handler.parent()?.open()
            }
        }
    }

    suspend fun requestChoice(player: Player, receiver: Player, plotType: PlotType? = null, parent: InterfaceView? = null): PlotType? {
        val personalPlot = receiver.personalPlot()
        val guildPlot = receiver.guildPlot()

        if (plotType != null) { // Handle already specified plot type
            when (plotType) {
                PlotType.PERSONAL -> { if (personalPlot == null) {
                    return null
                } }
                PlotType.GUILD -> { if (guildPlot == null) {
                    return null
                } }
            }
            return plotType
        }

        if ((personalPlot == null) && (guildPlot == null)) { // If no plots are available, return null
            return null
        }

        if ((personalPlot == null) xor (guildPlot == null)) { // If only one plot type is available, return that, no need to open menu
            return personalPlot?.let { PlotType.PERSONAL } ?: PlotType.GUILD
        }

        // Store the receiver and request for the plot type
        receiverChoice[player] = receiver
        val request = CompletableDeferred<PlotType?>()
        plotTypeChoice[player] = request
        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            plotTypeChoice.remove(player) // Remove the request because it's been fulfilled already
        }
    }
}