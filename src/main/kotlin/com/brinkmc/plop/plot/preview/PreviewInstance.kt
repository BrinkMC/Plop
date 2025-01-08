package com.brinkmc.plop.plot.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.stacksFromBase64
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory
import java.util.UUID

class PreviewInstance(
    override val plugin: Plop, val player: UUID, val previousLocation: Location, val previousInventory: String
): Addon {

    val bukkitPlayer: Player

    lateinit var viewPlot: StringLocation

    fun getInventory(): PlayerInventory {
        bukkitPlayer.inventory.contents = stacksFromBase64(previousInventory)
    }

}