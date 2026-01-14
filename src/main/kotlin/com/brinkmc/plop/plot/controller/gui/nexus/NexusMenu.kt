package com.brinkmc.plop.plot.controller.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Gui
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.util.CoroutineUtils.async
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class NexusMenu(override val plugin: Plop): Gui {

    private suspend fun getLogButton(playerId: UUID? = null, plotId: UUID? = null): ItemStack {
        return messages.getItem(
            ItemKey.BACK,
            MessageKey.MENU_NEXUS_PLOTLOG_NAME,
            MessageKey.MENU_NEXUS_PLOTLOG_DESC,
            playerId = playerId,
            plotId = plotId
        )
    }

    private suspend fun getTotemButton(playerId: UUID? = null, plotId: UUID? = null): ItemStack {
        return messages.getItem(
            ItemKey.NEXUS_TOTEM,
            MessageKey.MENU_NEXUS_TOTEM_NAME,
            MessageKey.MENU_NEXUS_TOTEM_DESC,
            playerId = playerId,
            plotId = plotId
        )
    }

    private suspend fun getUpgradeButton(playerId: UUID? = null, plotId: UUID? = null): ItemStack {
        return messages.getItem(
            ItemKey.NEXUS_UPGRADE,
            MessageKey.MENU_NEXUS_UPGRADE_NAME,
            MessageKey.MENU_NEXUS_UPGRADE_DESC,
            playerId = playerId,
            plotId = plotId
        )
    }

    private suspend fun inventory(vararg args: Any): ChestInterface = buildChestInterface {

        //setupBackButton()
        val playerId = args[0] as UUID
        val plotId = args[1] as UUID


        setupLogButton(playerId, plotId)
        setupTotemButton(playerId, plotId)
        setupUpgradeButton(playerId, plotId)

        return@buildChestInterface
    }

    fun ChestInterfaceBuilder.setupLogButton(playerId: UUID, plotId: UUID) {
        withTransform { pane, view ->
            pane[4, 4] = StaticElement(
                drawable(
                    getLogButton(playerId = playerId, plotId = plotId)
                )
            ) { _ ->
                plugin.async {
                    menuService.plotLogMenu.open(playerId, view, plotId)
                }
            }
        }
    }

    fun ChestInterfaceBuilder.setupTotemButton(playerId: UUID, plotId: UUID) {
        withTransform { pane, view ->
            pane[4, 5] = StaticElement(
                drawable(
                    getTotemButton(playerId = playerId, plotId = plotId)
                )
            ) { _ ->
                plugin.async {
                    menuService.totemMenu.open(playerId, view, plotId)
                }
            }
        }
    }

    fun ChestInterfaceBuilder.setupUpgradeButton(playerId: UUID, plotId: UUID) {
        withTransform { pane, view ->
            pane[4, 3] = StaticElement(
                drawable(
                    getUpgradeButton(playerId = playerId, plotId = plotId)
                )
            ) { _ ->
                plugin.async {
                    menuService.upgradeMenu.open(playerId, view, plotId)
                }
            }
        }
    }

    override suspend fun open(playerId: UUID, view: InterfaceView?, vararg args: Any): InterfaceView {
        return playerService.openMenu(inventory(playerId, *args), playerId, view)!!
    }
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