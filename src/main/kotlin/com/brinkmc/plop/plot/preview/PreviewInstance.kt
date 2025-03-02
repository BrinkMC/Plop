package com.brinkmc.plop.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.collection.Node
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.Base64
import java.util.UUID

/*
State handler, no player facing frills, just raw action
 */
class PreviewInstance(
    override val plugin: Plop,
    var type: PlotType,
    var viewPlot: Node<StringLocation>,
    val player: UUID,
    val previousLocation: Location,
    val previousInventory: Array<ItemStack?>,
    var interfaceView: InterfaceView? = null
): Addon {

    val bukkitPlayer: Player?
        get() = player.player()

    suspend fun teleportToViewPlot() {
        bukkitPlayer?.player?.teleportAsync(viewPlot.value.toLocation().getSafeDestination() ?: viewPlot.value.toLocation())// Hopefully teleports them to next plot
        syncScope {
            bukkitPlayer?.allowFlight = true
            bukkitPlayer?.isFlying = true
            viewPlot.value.free = false
        }

    }

    suspend fun returnTeleport() {
        syncScope {
            bukkitPlayer?.allowFlight = false
            bukkitPlayer?.isFlying = false
        }
        bukkitPlayer?.player?.teleportAsync(previousLocation) // Only if they end preview normally
    }

    suspend fun returnInventory() {
        syncScope {
            bukkitPlayer?.inventory?.contents = previousInventory
        }
    }
}