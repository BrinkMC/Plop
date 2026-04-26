package com.brinkmc.plop.plot.controller.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotLog
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.gui.Gui
import com.brinkmc.plop.shared.base.gui.PaginatedGui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.brinkmc.plop.shared.util.CoroutineUtils.sync
import com.brinkmc.plop.shared.util.PaginationContext
import com.noxcrew.interfaces.click.ClickHandler
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.Element
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ContainerInterface
import com.noxcrew.interfaces.interfaces.ContainerInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PlotLogMenu(override val plugin: Plop): PaginatedGui<PlotLog> {

    private suspend fun getGenericLog(log: PlotLog): ItemStack {
        return messages.getItem(
            log.action.item,
            log.action.logName,
            log.action.logDesc,
        )
    }

    private suspend fun inventory(vararg args: Any): ContainerInterface.Simple = buildChestInterface {

        val playerId = args[0] as UUID
        val plotId = args[1] as UUID

        val listOfLogs = plotLogService.getLogs(plotId)

        setupPagedGrid(
            PaginationContext(
                listOfLogs, 0..5
            ))
        { log, _ ->
            StaticElement(
                drawable(
                    getGenericLog(log)
                )
            )
        }

        setupBackButton(5, 4)

        return@buildChestInterface
    }

    override suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): InterfaceView {
        return playerService.openMenu(inventory(playerId, *args), playerId, view)!!
    }
}