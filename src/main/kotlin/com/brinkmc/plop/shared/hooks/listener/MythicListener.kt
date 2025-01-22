package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythiccrucible.events.MythicFurniturePlaceEvent
import io.lumine.mythiccrucible.events.MythicFurnitureRemoveEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicListener(override val plugin: Plop): Addon, State, Listener {
    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler // Handle nexus placement + totem placement and registering in system
    suspend fun onMythicFurniturePlace(mythicFurniturePlaceEvent: MythicFurniturePlaceEvent) {

    }

    @EventHandler
    suspend fun onMythicFurnitureRemove(mythicFurnitureRemoveEvent: MythicFurnitureRemoveEvent) {

    }
}