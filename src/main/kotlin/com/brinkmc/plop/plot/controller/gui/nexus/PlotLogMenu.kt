package com.brinkmc.plop.plot.controller.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Gui
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import java.util.UUID

class PlotLogMenu(override val plugin: Plop): Addon, Gui {

    private val inventory = buildChestInterface {

    }

    override suspend fun open(
        playerId: UUID,
        view: InterfaceView?,
        vararg args: Any
    ): InterfaceView {
        TODO("Not yet implemented")
    }
}