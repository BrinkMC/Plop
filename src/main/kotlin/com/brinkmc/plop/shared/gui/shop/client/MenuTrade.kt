package com.brinkmc.plop.shared.gui.shop.client

import com.brinkmc.plop.shared.util.RegistrableInterface
import com.noxcrew.interfaces.drawable.Drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.Interface
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Material
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerInteractEvent

//class MenuTrade: RegistrableInterface {
//
//    override suspend fun create(): Interface<*, *> = buildCombinedInterface {
//        rows = 1
//
//        withTransform { pane, _ ->
//            pane[3, 3] = StaticElement(Drawable.drawable(Material.STICK)) { (player) -> plugin.async {
//
//            }}
//        }
//    }
//}