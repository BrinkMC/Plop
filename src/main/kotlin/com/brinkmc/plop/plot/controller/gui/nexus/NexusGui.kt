package com.brinkmc.plop.plot.controller.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Gui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player

class NexusGui(override val plugin: Plop): Gui {

}


//    private val inventory = buildChestInterface {
//        onlyCancelItemInteraction = false
//        prioritiseBlockInteractions = false
//        rows = 6
//
//        withTransform { pane, view ->
//            // Determine plot
//            val plot = view.player.getCurrentPlot() ?: return@withTransform
//
//            // Main overview button
//            val individualMainOverview = ItemKey.NEXUS_OVERVIEW.get(
//                MessageKey.MENU_NEXUS_ICON_NAME,
//                MessageKey.MENU_NEXUS_ICON_DESC
//            )
//                .setSkull(plot.owner)
//                .description("nexus.main.overview.description",
//                    player = view.player,
//                    plot = plot
//                )
//
//            pane[2, 4] = StaticElement(drawable(individualMainOverview)) { (player) -> plugin.async {
//                open(player)
//            } }
//
//            // Upgrades button
//            pane[2, 2] = StaticElement(drawable(UPGRADES)) { (player) -> plugin.async {
//                plugin.menus.nexusUpgradeMenu.open(player, view)
//            } }
//
//            // Totems button
//            pane[2, 6] = StaticElement(drawable(TOTEMS)) { (player) -> plugin.async {
//                plugin.menus.nexusTotemsMenu.open(player, view)
//            } }
//        }
//    }
//
//    suspend fun open(player: Player, prev: InterfaceView? = null) {
//        inventory.open(player, prev)
//    }
//}