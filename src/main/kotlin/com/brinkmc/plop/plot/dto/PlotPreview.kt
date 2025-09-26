package com.brinkmc.plop.plot.dto

import com.brinkmc.plop.plot.constant.StringLocation
import com.brinkmc.plop.shared.util.type.Node
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class PlotPreview(
    val id: UUID, // Player's UUID
    val savedLocation: Location,
    val savedInventory: Array<ItemStack?>,

    private var _world: String,
    private var _previewPlot: Node<StringLocation>,
    private var _interfaceView: InterfaceView
) {
    val mutex = Mutex()

    val world: String
        get() = _world

    val previewPlot: Node<StringLocation>
        get() = _previewPlot

    val interfaceView: InterfaceView
        get() = _interfaceView

    suspend fun setPreviewPlot(previewPlot: Node<StringLocation>) = mutex.withLock {
        _previewPlot = previewPlot
    }

    suspend fun setInterfaceView(interfaceView: InterfaceView) = mutex.withLock {
        _interfaceView = interfaceView
    }

    suspend fun setWorld(world: String) = mutex.withLock {
        _world = world
    }
}
