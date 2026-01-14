package com.brinkmc.plop.plot.controller.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Gui
import com.noxcrew.interfaces.view.InterfaceView
import java.util.UUID

class TotemMenu(override val plugin: Plop): Gui {



    override suspend fun open(
        playerId: UUID,
        view: InterfaceView?,
        vararg args: Any
    ): InterfaceView {
        TODO("Not yet implemented")
    }


}
//    val BASE_TOTEM: ItemStack
//        get() = ItemStack(Material.TOTEM_OF_UNDYING)
//
//    val name = "nexus.totem.list.name"
//    val desc = "nexus.totem.list.desc"
//
//    val BACK: ItemStack
//        get() = ItemStack(Material.REDSTONE)
//        .name("menu.back")
//
//    // Only one list
//    private val inventory = buildChestInterface {
//        onlyCancelItemInteraction = false
//        prioritiseBlockInteractions = false
//        rows = 6
//
//        withTransform { pane, view ->
//            // Determine plot
//            val plot = view.player.getCurrentPlot() ?: return@withTransform
//
//            val totemItems = plot.totem.totems.map { totem ->
//
//                val tags = Placeholder.component(
//                    "totem",
//                    Component.text(totem.totemType.toString().lowercase())
//                )
//
//                BASE_TOTEM
//                    .name("nexus.totem.list.item.name", args = arrayOf(tags))
//                    .description("nexus.totem.list.item.desc", args = arrayOf(tags))
//            }
//
//            // Totem list populate for that
//            var k = 0
//            for (i in 0..8) {
//                for (j in 0..5) {
//                    if (k < totemItems.size) {
//                        val totem = totemItems[k] // Populate every item of pane with totem till no more
//                        k++
//                        pane[i, j] = StaticElement(
//                            drawable(
//                                totem
//                            )
//                        ) // Potential for a click event in future if required
//                    }
//                }
//            }
//
//            // Back button
//            pane[5, 8] = StaticElement(drawable(BACK)) { (player) -> plugin.async {
//                view.back()
//            } }
//
//        }
//    }
//
//    suspend fun open(player: Player, prev: InterfaceView? = null) {
//        inventory.open(player, prev)
//    }
//}