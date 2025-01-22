package com.brinkmc.plop.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.gui.HotbarPreview
import com.brinkmc.plop.shared.util.GuiUtils.stacksFromBase64
import com.brinkmc.plop.shared.util.collection.Node
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID

/*
State handler, no player facing frills, just raw action
 */
class PreviewInstance(
    override val plugin: Plop,
    val player: UUID,
    val previousLocation: Location,
    val interfaceView: InterfaceView
): Addon {

    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(player)

    lateinit var viewPlot: Node<StringLocation>
    lateinit var type: PlotType

    fun teleportToViewPlot() {
        bukkitPlayer?.player?.teleport(viewPlot.value.toLocation()) // Hopefully teleports them to next plot
    }

    fun returnTeleport() {
        bukkitPlayer?.player?.teleport(previousLocation) // Only if they end preview normally
    }
}