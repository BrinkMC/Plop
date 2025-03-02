package com.brinkmc.plop.shared.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuTotemList(override val plugin: Plop): Addon {

    val BASE_TOTEM = ItemStack(Material.TOTEM_OF_UNDYING)

    val name = "nexus.totem.list.name"
    val desc = "nexus.totem.list.desc"

    val BACK = ItemStack(Material.REDSTONE)
        .name("menu.back")

    // Only one list
    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 6

        withTransform { pane, view ->
            // Determine plot
            val plot = view.player.getCurrentPlot() ?: return@withTransform

            val totemItems = plot.totem.totems.map { totem ->

                val tags = Placeholder.component(
                    "totem",
                    Component.text(totem.totemType.toString().lowercase())
                )

                BASE_TOTEM.clone()
                    .name("nexus.totem.list.item.name", args = arrayOf(tags))
                    .description("nexus.totem.list.item.desc", args = arrayOf(tags))
            }

            // Totem list populate for that
            var k = 0
            for (i in 0..8) {
                for (j in 0..5) {
                    while (k < totemItems.size) {
                        val totem = totemItems[k] // Populate every item of pane with totem till no more
                        k++
                        pane[i, j] = StaticElement(
                            drawable(
                                totem
                            )
                        ) // Potential for a click event in future if required
                    }
                }
            }

            // Back button
            pane[5, 8] = StaticElement(drawable(BACK)) { (player) -> plugin.async {
                view.back()
            } }

        }
    }

    suspend fun open(player: Player, prev: InterfaceView? = null) {
        inventory.open(player, prev)
    }
}