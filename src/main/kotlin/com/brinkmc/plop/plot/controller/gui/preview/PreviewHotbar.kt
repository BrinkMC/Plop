package com.brinkmc.plop.plot.controller.gui.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.Gui
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildPlayerInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import com.noxcrew.interfaces.view.PlayerInterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.seconds

class PreviewHotbar(override val plugin: Plop): Addon, Gui {
    // Requires a cooldown




//    private val cooldownHandle = Cooldown(plugin, 3.seconds)
//
//    // Inventory items
//    val BACK_BUTTON: ItemStack
//        get() = ItemStack(Material.ARROW)
//        .name("preview.back-button.name")
//        .description("preview.back-button.desc")
//
//    val FORWARD_BUTTON: ItemStack
//        get() = ItemStack(Material.ARROW)
//        .name("preview.forward-button.name")
//        .description("preview.forward-button.desc")
//
//    val CONFIRM_BUTTON: ItemStack
//        get() = ItemStack(Material.EMERALD)
//        .name("preview.confirm-button.name")
//        .description("preview.confirm-button.desc")
//
//    val TOGGLE_BUTTON_GUILD: ItemStack
//        get() = ItemStack(Material.PLAYER_HEAD)
//        .name("preview.toggle-button.guild-name")
//        .description("preview.toggle-button.guild-desc")
//
//    val TOGGLE_BUTTON_PERSONAL: ItemStack
//        get() = ItemStack(Material.PLAYER_HEAD)
//        .name("preview.toggle-button.personal-name")
//        .description("preview.toggle-button.personal-desc")
//
//    val CANCEL_BUTTON: ItemStack
//        get() = ItemStack(Material.REDSTONE)
//        .name("preview.cancel-button.name")
//        .description("preview.cancel-button.desc")
//
//
//
//    private val inventory = buildPlayerInterface {
//        onlyCancelItemInteraction = false
//        prioritiseBlockInteractions = false
//
//        val plotTypeProperty = interfaceProperty(PlotType.PERSONAL) // Set initial property
//        var plotType by plotTypeProperty
//
//        withTransform { pane, view ->
//            plotType = updateType(view) // Update plot type
//
//            pane.hotbar[0] = StaticElement(drawable(BACK_BUTTON)) { (player) -> plugin.async {
//                if (cooldownHandle.bool(player)) return@async
//                plots.previewHandler.previousPlot(player.uniqueId)
//            }}
//            pane.hotbar[1] = StaticElement(drawable(CONFIRM_BUTTON)){ (player) -> plugin.async {
//                if (cooldownHandle.bool(player)) return@async
//                plots.claimHandler.initiateClaim(player.uniqueId, plotType)
//            }}
//            pane.hotbar[7] = StaticElement(drawable(CANCEL_BUTTON)) { (player) -> plugin.async {
//                if (cooldownHandle.bool(player)) return@async
//                plots.previewHandler.endPreview(player.uniqueId)
//            }}
//            pane.hotbar[8] = StaticElement(drawable(FORWARD_BUTTON)) { (player) -> plugin.async {
//                if (cooldownHandle.bool(player)) return@async
//                plots.previewHandler.nextPlot(player.uniqueId)
//            }}
//        }
//
//        withTransform(plotTypeProperty) { pane, view ->
//            val personalPlot = view.player.personalPlot()
//            val guildPlot = view.player.guildPlot()
//
//            val individualPersonal = TOGGLE_BUTTON_PERSONAL.setSkull(guildPlot?.owner) // Set to the skull meta
//            val individualGuild = TOGGLE_BUTTON_GUILD.setSkull(personalPlot?.owner) // Set to the skull meta
//
//            when (plotType) { // Determine the toggle button orientation
//                PlotType.PERSONAL -> { pane.hotbar[3] = StaticElement(drawable(individualPersonal)) { (player) -> plugin.async {
//                    if (cooldownHandle.bool(player)) return@async
//                    plots.previewHandler.switchPreview(player.uniqueId) // Update preview
//                    plotType = updateType(view)
//                    view.redrawComplete()
//                } } }
//                PlotType.GUILD -> { pane.hotbar[3] = StaticElement(drawable(individualGuild)) { (player) -> plugin.async {
//                    if (cooldownHandle.bool(player)) return@async
//                    plots.previewHandler.switchPreview(player.uniqueId) // Update preview
//                    plotType = updateType(view)
//                    view.redrawComplete()
//                } } }
//            }
//        }
//    }
//
//    suspend fun open(player: Player, parent: InterfaceView? = null): PlayerInterfaceView {
//        return inventory.open(player, parent)
//    }
//
//    private fun updateType(view: InterfaceView): PlotType {
//        return plots.previewHandler.getPreview(view.player.uniqueId)?.type ?: throw Exception("Failed to get preview to update type")// Default to personal otherwise
//    }
}
