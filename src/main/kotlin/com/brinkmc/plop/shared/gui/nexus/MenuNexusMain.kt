package com.brinkmc.plop.shared.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.GuiUtils.description
import com.brinkmc.plop.shared.util.GuiUtils.name
import com.brinkmc.plop.shared.util.GuiUtils.setSkull
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuNexusMain(override val plugin: Plop): Addon {

    val MAIN_OVERVIEW = ItemStack(Material.PLAYER_HEAD)
        .name(lang.get("nexus.main.overview.name"))

    val UPGRADES = ItemStack(Material.SPLASH_POTION)
        .name(lang.get("nexus.upgrades.name"))

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 6

        withTransform { pane, view ->
            // Determine plot
            val plot = view.player.getCurrentPlot() ?: return@withTransform

            // Main overview button
            val individualMainOverview = MAIN_OVERVIEW.clone()
                .setSkull(plot.owner)
                .description(lang.get("nexus.main.overview.description", view.player, plot))

            pane[2, 4] = StaticElement(drawable(individualMainOverview)) { (player) -> plugin.async {
                open(player)
            } }


        }
    }

    suspend fun open(player: Player, prev: InterfaceView? = null) {
        inventory.open(player, prev)
    }
}