package com.brinkmc.plop.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.plotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.hotbar.preview.HotbarPreview
import com.brinkmc.plop.shared.util.collection.Node
import com.brinkmc.plop.shared.util.stacksFromBase64
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
    val previousInventory: String
): Addon {

    val hotbarPreview = HotbarPreview(plugin, player)

    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(player)

    lateinit var viewPlot: Node<StringLocation>
    lateinit var type: plotType


    fun returnInventory() { // Reset player inventory back to normal
        bukkitPlayer?.inventory?.contents = stacksFromBase64(previousInventory)
    }

    fun setHotbarInventory() {
        bukkitPlayer?.inventory?.clear() // Clear previous inventory

        bukkitPlayer?.inventory?.setItem(0, hotbarPreview.BACK_BUTTON)
        bukkitPlayer?.inventory?.setItem(1, hotbarPreview.BACK_BUTTON)
        bukkitPlayer?.inventory?.setItem(7, hotbarPreview.BACK_BUTTON)
        bukkitPlayer?.inventory?.setItem(8, hotbarPreview.BACK_BUTTON) // Set buttons
    }

    fun teleportToViewPlot() {
        bukkitPlayer?.player?.teleport(viewPlot.value.toLocation()) // Hopefully teleports them to next plot
    }

    fun returnTeleport() {
        bukkitPlayer?.player?.teleport(previousLocation) // Only if they end preview normally
    }

}