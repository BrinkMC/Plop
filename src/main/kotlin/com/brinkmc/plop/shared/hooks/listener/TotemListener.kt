package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.structure.TotemType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import io.lumine.mythiccrucible.events.MythicFurniturePlaceEvent
import io.lumine.mythiccrucible.events.MythicFurnitureRemoveEvent
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.block.MoistureChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class TotemListener(override val plugin: Plop): Addon, State, Listener {

    lateinit var plotWorlds: List<World>

    override suspend fun load() {
        plotWorlds = plots.handler.getPlotWorlds()
    }

    override suspend fun kill() {}

    @EventHandler // Handle nexus placement + totem placement and registering in system
    suspend fun onMythicFurniturePlace(mythicFurniturePlaceEvent: MythicFurniturePlaceEvent) {
        // Register a totem if it's placed
        mythicFurniturePlaceEvent.furnitureItemContext.it
    }

    @EventHandler
    suspend fun onMythicFurnitureRemove(mythicFurnitureRemoveEvent: MythicFurnitureRemoveEvent) {

    }

    @EventHandler
    suspend fun on(event: BlockBurnEvent) {
        val location = event.block.location
        if (plotWorlds.contains(location.world)) {
            return
        }

        val potentialPlot = location.getCurrentPlot()

        if (potentialPlot == null) {
            return
        }

        if (!potentialPlot.totem.getTypes().contains(TotemType.FIRE_SPREAD)) {
            return
        }

        event.isCancelled = true

        if (!potentialPlot.totem.enableLightning) {
            return
        }

        // Summon lightning on totem for effect to signify it worked
        val totem = potentialPlot.totem.getTotem(TotemType.FIRE_SPREAD)

        syncScope {
            totem?.location?.world?.strikeLightningEffect(totem.location)
        }
    }

    @EventHandler
    suspend fun on(event: BlockFadeEvent) {
        val location = event.block.location
        if (plotWorlds.contains(location.world)) {
            return
        }

        val potentialPlot = location.getCurrentPlot()

        if (potentialPlot == null) {
            return
        }

        if (!potentialPlot.totem.getTypes().contains(TotemType.ICE_MELT)) {
            return
        }

        event.isCancelled = true

        if (!potentialPlot.totem.enableLightning) {
            return
        }

        // Summon lightning on totem for effect to signify it worked
        val totem = potentialPlot.totem.getTotem(TotemType.FIRE_SPREAD)

        syncScope {
            totem?.location?.world?.strikeLightningEffect(totem.location)
        }
    }

    @EventHandler
    suspend fun on(event: LeavesDecayEvent) {
        val location = event.block.location
        if (plotWorlds.contains(location.world)) {
            return
        }

        val potentialPlot = location.getCurrentPlot()

        if (potentialPlot == null) {
            return
        }

        if (!potentialPlot.totem.getTypes().contains(TotemType.LEAF_DECAY)) {
            return
        }

        event.isCancelled = true

        if (!potentialPlot.totem.enableLightning) {
            return
        }

        // Summon lightning on totem for effect to signify it worked
        val totem = potentialPlot.totem.getTotem(TotemType.FIRE_SPREAD)

        syncScope {
            totem?.location?.world?.strikeLightningEffect(totem.location)
        }
    }

    @EventHandler
    suspend fun on(event: BlockFromToEvent) {
        val from = event.block.location
        val to = event.toBlock.location

        // Detect if it is water flow event
        if (to.block.type != Material.WATER || to.block.type != Material.LAVA) {
            return
        }
    }
}