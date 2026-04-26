package com.brinkmc.plop.plot.controller.gui.selector

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.gui.Gui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.DeferredRequest
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ContainerInterface
import com.noxcrew.interfaces.interfaces.ContainerInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PlotTypeMenu(override val plugin: Plop): Addon, Gui {

    val requests = DeferredRequest<UUID, PlotType?>()

    private suspend fun getClosedButton(): ItemStack {
        return messages.getItem(
            ItemKey.CLOSED_PLOT,
            MessageKey.MENU_PLOTSELECTION_CLOSED_NAME,
            MessageKey.MENU_PLOTSELECTION_CLOSED_DESC
        )
    }

    private suspend fun getPersonalButton(playerId: UUID, plotId: UUID?): ItemStack {
        return when (plotId == null) {
            true -> {
                messages.getItem(
                    ItemKey.PERSONAL_PLOT,
                    MessageKey.MENU_PLOTSELECTION_PERSONAL_NAME,
                    MessageKey.MENU_PLOTSELECTION_PERSONAL_DESC,
                )
            }
            false -> {
                messages.setSkull(
                    messages.getItem(
                        ItemKey.PLAYER_HEAD,
                        MessageKey.MENU_PLOTSELECTION_PERSONAL_NAME,
                        MessageKey.MENU_PLOTSELECTION_PERSONAL_DESC,
                        playerId = playerId,
                        plotId = plotId
                    ),
                    plotId
                )
            }
        }
    }

    private suspend fun getGuildButton(playerId: UUID, plotId: UUID?): ItemStack {
        return when (plotId == null) {
            true -> {
                messages.getItem(
                    ItemKey.GUILD_PLOT,
                    MessageKey.MENU_PLOTSELECTION_GUILD_NAME,
                    MessageKey.MENU_PLOTSELECTION_GUILD_DESC,
                )
            }
            false -> {
                messages.setSkull(
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
        }
    }

    private suspend fun inventory(vararg args: Any?): ContainerInterface.Simple = buildChestInterface {

        val playerId = args[0] as UUID
        val preTargetId = args.getOrNull(1) as? UUID

        // Determine the subject of the lookup
        val targetId = preTargetId ?: playerId
        val isCreationMode = preTargetId == null

        val personalPlotId = plotService.getPlotId(targetId, PlotType.PERSONAL)
        val guildPlotId = plotService.getPlotId(targetId, PlotType.GUILD)

        // Flat logic: (Creation == Plot Missing)
        if (isCreationMode == (personalPlotId == null)) {
            setupPersonalButton(playerId, personalPlotId)
        }

        if (isCreationMode == (guildPlotId == null)) {
            setupGuildButton(playerId, guildPlotId)
        }
    }

    fun ContainerInterfaceBuilder.Simple.setupPersonalButton(playerId: UUID, plotId: UUID?) {
        withTransform { pane, view ->
            pane[5, 1] = StaticElement(
                drawable(
                    if (plotId == null) {
                        getPersonalButton(playerId, plotId)
                    }
                    else {
                        if (!plotVisitService.isVisitingEnabled(plotId) && !plotService.isPlotOwner(plotId, playerId)) {
                            getClosedButton()
                        }
                        else {
                            getPersonalButton(playerId, plotId)
                        }
                    }
                )
            ) { _ ->
                plugin.async {
                    requests.fulfill(playerId, PlotType.PERSONAL)
                    view.close()
                }
            }
        }
    }

    fun ContainerInterfaceBuilder.Simple.setupGuildButton(playerId: UUID, plotId: UUID?) {
        withTransform { pane, view ->
            pane[3, 1] = StaticElement(
                drawable(
                    if (plotId == null) {
                        getGuildButton(playerId, plotId)
                    }
                    else {
                        if (!plotVisitService.isVisitingEnabled(plotId) && !plotService.isPlotOwner(plotId, playerId)) {
                            getClosedButton()
                        }
                        else {
                            getGuildButton(playerId, plotId)
                        }
                    }
                )
            ) { _ ->
                plugin.async {
                    requests.fulfill(playerId, PlotType.GUILD)
                    view.close()
                }
            }
        }
    }

    suspend fun request(playerId: UUID, view: InterfaceView?, targetId: UUID?): PlotType? {
        requests.request(playerId)

        playerService.openMenu(inventory(playerId, targetId), playerId, view)

        return requests.await(playerId)
    }

    // Do not use this function
    override suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): InterfaceView? {
        return null
    }
}