package com.brinkmc.plop.plot.controller.gui.selector

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Gui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.DeferredRequest
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture

class PlotTypeMenu(override val plugin: Plop): Addon, Gui {

    val requests = DeferredRequest<UUID, PlotType?>()

    private suspend fun getPersonalButton(playerId: UUID, plotId: UUID): ItemStack {
        return messages.setSkull(
            messages.getItem(
                ItemKey.PLAYER_HEAD,
                MessageKey.MENU_PLOTSELECTION_PERSONAL_NAME,
                MessageKey.MENU_PLOTSELECTION_PERSONAL_DESC,
                playerId = playerId,
                plotId = plotId
            ),
            playerId
        )
    }

    private suspend fun getGuildButton(playerId: UUID, plotId: UUID): ItemStack {
        return messages.setSkull(
            messages.getItem(
                ItemKey.PLAYER_HEAD,
                MessageKey.MENU_PLOTSELECTION_GUILD_NAME,
                MessageKey.MENU_PLOTSELECTION_GUILD_DESC,
                playerId = playerId,
                plotId = plotId
            ),
            plotId
        )
    }

    private suspend fun inventory(vararg args: Any): ChestInterface = buildChestInterface {

        val playerId = args[0] as UUID // Selected player

        val personalPlotId = plotService.getPlotId(playerId, PlotType.PERSONAL)
        val guildPlotId = plotService.getPlotId(playerId, PlotType.GUILD)



        if (personalPlotId != null) {
            setupPersonalButton(playerId, personalPlotId)
        }

        if (guildPlotId != null) {
            setupGuildButton(playerId, guildPlotId)
        }
    }

    fun ChestInterfaceBuilder.setupPersonalButton(playerId: UUID, plotId: UUID) {
        withTransform { pane, view ->
            pane[5, 1] = StaticElement(
                drawable(
                    getGuildButton(playerId, plotId),
                )
            ) { _ ->
                plugin.async {
                    requests.fulfill(playerId, PlotType.PERSONAL)
                    view.close()
                }
            }
        }
    }

    fun ChestInterfaceBuilder.setupGuildButton(playerId: UUID, plotId: UUID) {
        withTransform { pane, view ->
            pane[3, 1] = StaticElement(
                drawable(
                    getGuildButton(playerId, plotId),
                )
            ) { _ ->
                plugin.async {
                    requests.fulfill(playerId, PlotType.GUILD)
                    view.close()
                }
            }
        }
    }

    suspend fun request(playerId: UUID, view: InterfaceView?, targetId: UUID): PlotType? {
        requests.request(playerId)

        playerService.openMenu(inventory(targetId), playerId, view)

        return requests.await(playerId)
    }

    override suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): InterfaceView? {
        val targetId = args.getOrNull(0) as? UUID ?: return null

        val personalPlotId = plotService.getPlotId(targetId, PlotType.PERSONAL)
        val guildPlotId = plotService.getPlotId(targetId, PlotType.GUILD)

        if (personalPlotId == null && guildPlotId == null) {
            requests.fulfill(playerId, null)
            return null
        }
        if (personalPlotId == null) {
            requests.fulfill(playerId, PlotType.GUILD)
            return null
        }
        if (guildPlotId == null) {
            requests.fulfill(playerId, PlotType.PERSONAL)
            return null
        }

        return playerService.openMenu(inventory(targetId), playerId, view)!!
    }

}
//    private val receiverChoice = mutableMapOf<Player, Player>() // Pre-existing information before opening
//    private val plotTypeChoice = mutableMapOf<Player, CompletableDeferred<PlotType?>>() // Completable requests
//
//    // Inventory items
//    val GUILD_PLOT: ItemStack
//        get() = ItemStack(Material.PLAYER_HEAD)
//        .name("selection-other.toggle-guild.name")
//        .description("selection-other.toggle-guild.desc")
//
//    val PERSONAL_PLOT: ItemStack
//        get() = ItemStack(Material.PLAYER_HEAD)
//        .name("selection-other.toggle-personal.name")
//        .description("selection-other.toggle-personal.desc")
//
//    private val inventory = buildChestInterface {
//        onlyCancelItemInteraction = false
//        prioritiseBlockInteractions = false
//
//        rows = 1
//
//        withTransform { pane, view ->
//
//            val selectionPlot = receiverChoice[view.player]?.guildPlot() ?: return@withTransform
//
//            val individualGuildClone = GUILD_PLOT.setSkull(selectionPlot.owner)
//
//            pane[0, 3] = StaticElement(drawable(individualGuildClone)) { (player) -> plugin.async {
//                plotTypeChoice[player]?.complete(PlotType.GUILD)
//                view.close()
//            } }
//        }
//
//        withTransform { pane, view ->
//
//            val selectionPlot = receiverChoice[view.player]?.personalPlot() ?: return@withTransform
//
//            val individualPersonalClone = PERSONAL_PLOT.setSkull(selectionPlot.owner)
//
//            pane[0, 5] = StaticElement(drawable(individualPersonalClone)) { (player) -> plugin.async {
//                plotTypeChoice[player]?.complete(PlotType.PERSONAL)
//                view.close()
//            } }
//        }
//
//        withTransform { pane, view ->
//            if (pane[0,3] == null && pane[0,5] == null) {
//                view.close()
//            }
//
//            if (pane[0,3] == null && pane[0,5] != null) {
//                plotTypeChoice[view.player]?.complete(PlotType.PERSONAL)
//                view.close()
//            }
//
//            if (pane[0,3] != null && pane[0,5] == null) {
//                plotTypeChoice[view.player]?.complete(PlotType.GUILD)
//                view.close()
//            }
//        }
//
//        addCloseHandler { reasons, handler  ->
//            if (plotTypeChoice[handler.player]?.isCompleted == false) {
//                plotTypeChoice[handler.player]?.complete(null) // Finalise with a null if not completed
//            }
//
//            if (handler.parent() != null) {
//                handler.parent()?.open()
//                handler.parent()?.redrawComplete()
//            }
//        }
//    }
//
//    suspend fun requestChoice(player: Player, receiver: Player, plotType: PlotType? = null, parent: InterfaceView? = null): PlotType? {
//        val personalPlot = receiver.personalPlot()
//        val guildPlot = receiver.guildPlot()
//
//        if (plotType != null) { // Handle already specified plot type
//            when (plotType) {
//                PlotType.PERSONAL -> { if (personalPlot == null) {
//                    return null
//                } }
//                PlotType.GUILD -> { if (guildPlot == null) {
//                    return null
//                } }
//            }
//            return plotType
//        }
//
//        if ((personalPlot == null) && (guildPlot == null)) { // If no plots are available, return null
//            return null
//        }
//
//        if ((personalPlot == null) xor (guildPlot == null)) { // If only one plot type is available, return that, no need to open menu
//            return personalPlot?.let { PlotType.PERSONAL } ?: PlotType.GUILD
//        }
//
//        // Store the receiver and request for the plot type
//        receiverChoice[player] = receiver
//        val request = CompletableDeferred<PlotType?>()
//        plotTypeChoice[player] = request
//        try {
//            inventory.open(player, parent) // Open inventory to player to make a choice
//            return request.await()
//        } finally {
//            plotTypeChoice.remove(player) // Remove the request because it's been fulfilled already
//        }
//    }
//}
