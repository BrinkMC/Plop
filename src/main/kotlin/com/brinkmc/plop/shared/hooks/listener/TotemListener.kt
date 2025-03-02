package com.brinkmc.plop.shared.hooks.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.plot.plot.structure.TotemType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Locals.world
import io.lumine.mythiccrucible.events.MythicFurniturePlaceEvent
import io.lumine.mythiccrucible.events.MythicFurnitureRemoveEvent
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.block.MoistureChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.time.Duration.Companion.seconds

class TotemListener(override val plugin: Plop): Addon, State, Listener {

    lateinit var plotWorlds: List<World>

    override suspend fun load() {
        plotWorlds = listOfNotNull(
            plotConfig.getPlotWorld(PlotType.PERSONAL).world(),
            plotConfig.getPlotWorld(PlotType.GUILD).world()
        )
    }

    override suspend fun kill() {}

    @EventHandler // Handle nexus placement + totem placement and registering in system
    suspend fun onMythicFurniturePlace(event: MythicFurniturePlaceEvent) {
        // Register a totem if it's placed
        val key = event.furnitureItemContext.config.key

        if (!key.startsWith(totemConfig.totemId)) { // Ignore if not a totem
            return
        }

        val totemType = plots.totemHandler.getTotemTypeFromKey(key) ?: return

        // Determined that the totem is valid

        val player = event.player
        val plot = event.block.location.getCurrentPlot()

        if (plot == null) {
            event.isCancelled = true
            player.sendMiniMessage("plot.not-plot")
            return
        }

        if (!plot.owner.getPlayers().contains(player.uniqueId)) {
            event.isCancelled = true
            player.sendMiniMessage("plot.not-owner")
            return
        }

        if (!player.hasPermission("plop.totem.place")) {
            event.isCancelled = true
            player.sendMiniMessage("plot.no-permission")
            return
        }

        if (plot.totem.totems.size >= plot.totem.limit) {
            event.isCancelled = true
            player.sendMiniMessage("plot.totem.limit")
            return
        }

        plot.totem.addTotem(totemType, event.block.location)

        syncScope {
            // Send lightning
            event.block.location.world?.strikeLightningEffect(event.block.location)
        }
        player.sendMiniMessage("plot.totem.place-success")
    }

    @EventHandler
    suspend fun onMythicFurnitureRemove(event: MythicFurnitureRemoveEvent) {
        // Register a totem if it's placed
        val key = event.furnitureItemContext.config.key

        if (!key.startsWith(totemConfig.totemId)) {
            return
        }

        val totemType = plots.totemHandler.getTotemTypeFromKey(key) ?: return

        // Determined that the totem is valid
         val player = try {
            event.breaker as Player
         } catch (e: Exception) {
             logger.error("Mystical force deleted totem")
             return
         }
        val plot = event.furniture.location.getCurrentPlot()

        if (plot == null) {
            event.isCancelled = true
            player.sendMiniMessage("plot.not-plot")
            return
        }

        if (!plot.owner.getPlayers().contains(player.uniqueId)) {
            event.isCancelled = true
            player.sendMiniMessage("plot.not-owner")
            return
        }

        if (!player.hasPermission("plop.totem.break")) {
            event.isCancelled = true
            player.sendMiniMessage("plot.no-permission")
            return
        }

        if (plot.totem.totems.size >= plot.totem.limit) {
            event.isCancelled = true
            player.sendMiniMessage("plot.totem.limit")
            return
        }

        plot.totem.removeTotem(totemType, event.furniture.location.toLocation())

        syncScope {
            // Send lightning
            event.furniture.location.toLocation().world.strikeLightningEffect(event.furniture.location.toLocation())
        }
        player.sendMiniMessage("plot.totem.delete-success")
    }

    @EventHandler
    suspend fun on(event: BlockBurnEvent) {
        val location = event.block.location
        if (plotWorlds.contains(location.world)) {
            return
        }

        asyncScope {
            val potentialPlot = location.getCurrentPlot() ?: return@asyncScope

            if (!potentialPlot.totem.getTypes().contains(TotemType.FIRE_SPREAD)) {
                return@asyncScope
            }

            event.isCancelled = true

            if (!potentialPlot.totem.enableLightning) {
                return@asyncScope
            }

            // Summon lightning on totem for effect to signify it worked
            potentialPlot.totem.getTotem(TotemType.FIRE_SPREAD)?.strike()
        }
    }

    @EventHandler
    suspend fun on(event: BlockFadeEvent) {
        val location = event.block.location
        if (plotWorlds.contains(location.world)) {
            return
        }

        asyncScope {
            val potentialPlot = location.getCurrentPlot() ?: return@asyncScope

            if (!potentialPlot.totem.getTypes().contains(TotemType.ICE_MELT)) {
                return@asyncScope
            }

            event.isCancelled = true

            if (!potentialPlot.totem.enableLightning) {
                return@asyncScope
            }

            potentialPlot.totem.getTotem(TotemType.ICE_MELT)?.strike()
        }
    }

    @EventHandler
    suspend fun on(event: LeavesDecayEvent) {
        val location = event.block.location

        if (plotWorlds.contains(location.world)) {
            return
        }

        asyncScope {
            val potentialPlot = location.getCurrentPlot() ?: return@asyncScope

            if (!potentialPlot.totem.getTypes().contains(TotemType.LEAF_DECAY)) {
                return@asyncScope
            }

            event.isCancelled = true

            if (!potentialPlot.totem.enableLightning) {
                return@asyncScope
            }

            potentialPlot.totem.getTotem(TotemType.LEAF_DECAY)?.strike()
        }
    }

    @EventHandler
    suspend fun onWater(event: BlockFromToEvent) {
        val from = event.block.location
        val to = event.toBlock.location

        // Detect if it is water flow event
        if (to.block.type != Material.WATER) {
            return
        }

        asyncScope {
            val potentialPlot = to.getCurrentPlot() ?: return@asyncScope

            if (!potentialPlot.totem.getTypes().contains(TotemType.WATER_FLOW)) {
                return@asyncScope
            }

            event.isCancelled = true

            if (!potentialPlot.totem.enableLightning) {
                return@asyncScope
            }

            potentialPlot.totem.getTotem(TotemType.WATER_FLOW)?.strike()
        }
    }

    @EventHandler
    suspend fun onLava(event: BlockFromToEvent) {
        val from = event.block.location
        val to = event.toBlock.location

        // Detect if it is water flow event
        if (to.block.type != Material.LAVA) {
            return
        }

        asyncScope {
            val potentialPlot = to.getCurrentPlot() ?: return@asyncScope

            if (!potentialPlot.totem.getTypes().contains(TotemType.LAVA_FLOW)) {
                return@asyncScope
            }

            event.isCancelled = true

            if (!potentialPlot.totem.enableLightning) {
                return@asyncScope
            }

            potentialPlot.totem.getTotem(TotemType.LAVA_FLOW)?.strike()
        }
    }
}