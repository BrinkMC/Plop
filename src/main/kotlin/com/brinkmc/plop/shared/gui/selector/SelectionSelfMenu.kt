package com.brinkmc.plop.shared.gui.selector

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SelectionSelfMenu(override val plugin: Plop): Addon {

    private val plotTypeChoice = mutableMapOf<Player, CompletableDeferred<PlotType?>>() // Completable requests

    // Inventory items
    val GUILD_PLOT: ItemStack
        get() = ItemStack(Material.PLAYER_HEAD)
        .name("selection-self.toggle-guild.name")
        .description("selection-self.toggle-guild.desc")

    val PERSONAL_PLOT: ItemStack
        get() = ItemStack(Material.PLAYER_HEAD)
        .name("selection-self.toggle-personal.name")
        .description("selection-self.toggle-personal.desc")

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 1



        withTransform { pane, view ->
            val selectionPlot = view.player.guildPlot() ?: return@withTransform
            val individualGuildClone = GUILD_PLOT.setSkull(selectionPlot.owner)

            pane[0, 3] = StaticElement(drawable(individualGuildClone)) { (player) -> plugin.async {
                plotTypeChoice[player]?.complete(PlotType.GUILD)
                view.close()
            } }
        }

        withTransform { pane, view ->
            val selectionPlot = view.player.personalPlot() ?: return@withTransform
            val individualPersonalClone = PERSONAL_PLOT.setSkull(selectionPlot.owner)

            pane[0, 5] = StaticElement(drawable(individualPersonalClone)) { (player) -> plugin.async {
                plotTypeChoice[player]?.complete(PlotType.PERSONAL)
                view.close()
            } }
        }

        addCloseHandler { reasons, handler  ->
            if (plotTypeChoice[handler.player]?.isCompleted == false) {
                plotTypeChoice[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            if (handler.parent() != null) {
                handler.parent()?.open()
                handler.parent()?.redrawComplete()
            }
        }
    }

    suspend fun requestChoice(player: Player, plotType: PlotType? = null, parent: InterfaceView? = null): PlotType? {
        val personalPlot = player.personalPlot()
        val guildPlot = player.guildPlot()

        if (plotType != null) { // Handle already specified plot type
            when (plotType) {
                PlotType.PERSONAL -> { if (personalPlot == null) {
                    return null
                } }
                PlotType.GUILD -> { if (guildPlot == null) {
                    return null
                } }
            }
            return plotType
        }

        if ((personalPlot == null) && (guildPlot == null)) { // If no plots are available, return null
            return null
        }

        if ((personalPlot == null) xor (guildPlot == null)) { // If only one plot type is available, return that, no need to open menu
            return personalPlot?.let { PlotType.PERSONAL } ?: PlotType.GUILD
        }

        // Finally initiate request, hopefully should have both buttons present
        val request = CompletableDeferred<PlotType?>()
        plotTypeChoice[player] = request

        try {
            inventory.open(player, parent) // Open inventory to player to make a choice
            return request.await()
        } finally {
            plotTypeChoice.remove(player) // Remove the request because it's been fulfilled already
        }
    }
}
