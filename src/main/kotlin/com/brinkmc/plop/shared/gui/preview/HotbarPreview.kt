package com.brinkmc.plop.shared.gui.preview

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.Cooldown
import com.brinkmc.plop.shared.util.GuiUtils.description
import com.brinkmc.plop.shared.util.GuiUtils.name
import com.brinkmc.plop.shared.util.GuiUtils.setSkull
import com.brinkmc.plop.shared.util.RegistrableInterface
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.bukkit.launch
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.Interface
import com.noxcrew.interfaces.interfaces.InterfaceProperties
import com.noxcrew.interfaces.interfaces.buildPlayerInterface
import com.noxcrew.interfaces.properties.InterfaceProperty
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import com.noxcrew.interfaces.view.PlayerInterfaceView
import kotlinx.coroutines.launch
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit
import kotlin.collections.get
import kotlin.math.log
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

class HotbarPreview(override val plugin: Plop): Addon {
    // Requires a cooldown
    private val cooldownHandle = Cooldown(plugin, 3.seconds)

    // Inventory items
    val BACK_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.back-button.name"))
        .description(lang.get("preview.back-button.desc"))

    val FORWARD_BUTTON: ItemStack = ItemStack(Material.ARROW)
        .name(lang.get("preview.forward-button.name"))
        .description(lang.get("preview.forward-button.desc"))

    val CONFIRM_BUTTON: ItemStack = ItemStack(Material.EMERALD)
        .name(lang.get("preview.confirm-button.name"))
        .description(lang.get("preview.confirm-button.desc"))

    val TOGGLE_BUTTON_GUILD: ItemStack = ItemStack(Material.PLAYER_HEAD)
        .name(lang.get("preview.toggle-button.guild-name"))
        .description(lang.get("preview.toggle-button.guild-desc"))

    val TOGGLE_BUTTON_PERSONAL: ItemStack = ItemStack(Material.PLAYER_HEAD)
        .name(lang.get("preview.toggle-button.personal-name"))
        .description(lang.get("preview.toggle-button.personal-desc"))

    val CANCEL_BUTTON: ItemStack = ItemStack(Material.REDSTONE)
        .name(lang.get("preview.cancel-button.name"))
        .description(lang.get("preview.cancel-button.desc"))



    private val inventory = buildPlayerInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        val plotTypeProperty = interfaceProperty(PlotType.PERSONAL) // Set initial property
        var plotType by plotTypeProperty

        withTransform { pane, view ->
            plotType = updateType(view) // Update plot type

            pane.hotbar[0] = StaticElement(drawable(BACK_BUTTON)) { (player) -> plugin.async {
                if (cooldownHandle.bool(player)) return@async
                plots.previewHandler.previousPlot(player.uniqueId)
            }}
            pane.hotbar[1] = StaticElement(drawable(CONFIRM_BUTTON)){ (player) -> plugin.async {
                if (cooldownHandle.bool(player)) return@async
                plots.claimHandler.initiateClaim(player.uniqueId, plotType)
            }}
            pane.hotbar[7] = StaticElement(drawable(CANCEL_BUTTON)) { (player) -> plugin.async {
                if (cooldownHandle.bool(player)) return@async
                plots.previewHandler.endPreview(player.uniqueId)
            }}
            pane.hotbar[8] = StaticElement(drawable(FORWARD_BUTTON)) { (player) -> plugin.async {
                if (cooldownHandle.bool(player)) return@async
                plots.previewHandler.nextPlot(player.uniqueId)
            }}
        }

        withTransform(plotTypeProperty) { pane, view ->
            val selectionPlayer = view.player
            val selectionGuild = selectionPlayer.guild()

            val individualGuildClone = TOGGLE_BUTTON_GUILD.clone().setSkull(selectionGuild) // Set to the skull meta

            val individualPersonalClone = TOGGLE_BUTTON_PERSONAL.clone().setSkull(selectionPlayer) // Set to the skull meta

            when (plotType) { // Determine the toggle button orientation
                PlotType.PERSONAL -> { pane.hotbar[3] = StaticElement(drawable(individualPersonalClone)) { (player) -> plugin.async {
                    if (cooldownHandle.bool(player)) return@async
                    plots.previewHandler.switchPreview(player.uniqueId) // Update preview
                    plotType = updateType(view)
                    view.redrawComplete()
                } } }
                PlotType.GUILD -> { pane.hotbar[3] = StaticElement(drawable(individualGuildClone)) { (player) -> plugin.async {
                    if (cooldownHandle.bool(player)) return@async
                    plots.previewHandler.switchPreview(player.uniqueId) // Update preview
                    plotType = updateType(view)
                    view.redrawComplete()
                } } }
            }
        }
    }

    suspend fun open(player: Player, parent: InterfaceView? = null): PlayerInterfaceView {
        return inventory.open(player, parent)
    }

    private fun updateType(view: InterfaceView): PlotType {
        return plots.previewHandler.getPreview(view.player.uniqueId)?.type ?: throw Exception("Failed to get preview to update type")// Default to personal otherwise
    }
}
