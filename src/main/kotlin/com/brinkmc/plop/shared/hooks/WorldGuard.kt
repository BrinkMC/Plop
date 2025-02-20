package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hooks.Locals.localLocation
import com.brinkmc.plop.shared.hooks.Locals.localWorld
import com.brinkmc.plop.shared.hooks.Locals.world
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.domains.DefaultDomain
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.toString

class WorldGuard(override val plugin: Plop): Addon, State {

    private lateinit var worldGuardAPI: WorldGuard
    private val worldGuardPlatform: WorldGuardPlatform
        get() = worldGuardAPI.platform
    private val worldGuardRegionContainer: RegionContainer
        get() = worldGuardPlatform.regionContainer

    private val personalPlotWorld: com.sk89q.worldedit.world.World?
        get() = Bukkit.getWorld(plotConfig.getPlotWorld(PlotType.PERSONAL)!!)?.localWorld()

    private val guildPlotWorld: com.sk89q.worldedit.world.World?
        get() = Bukkit.getWorld(plotConfig.getPlotWorld(PlotType.GUILD)!!)?.localWorld()

    override suspend fun load() {
        worldGuardAPI = WorldGuard.getInstance()
    }

    override suspend fun kill() {

    }

    fun getRegion(world: String, uuid: UUID): ProtectedRegion? {
        val bukkitWorld = Bukkit.getWorld(world)

        if (bukkitWorld == null) {
            logger.error("Catastrophic error trying to get region")
            return null
        }

        return worldGuardRegionContainer.get(bukkitWorld.localWorld())?.regions?.get(uuid.toString())
    }

    fun getRegions(location: Location): Set<ProtectedRegion?>? {
        return worldGuardRegionContainer.createQuery().getApplicableRegions(location.localLocation()).regions
    }

    fun getRegions(player: Player): Set<ProtectedRegion?>? {
        return worldGuardRegionContainer.createQuery().getApplicableRegions(player.location.localLocation()).regions
    }

    fun getPlotRegions(plotType: PlotType): Map<String, ProtectedRegion> {
        return when (plotType) {
            PlotType.PERSONAL -> getPersonalPlotRegions()
            PlotType.GUILD -> getGuildPlotRegions()
        }
    }

    private fun getPersonalPlotRegions(): Map<String, ProtectedRegion> {
        return worldGuardRegionContainer.get(personalPlotWorld)?.regions ?: mapOf()
    }

    private fun getGuildPlotRegions(): Map<String, ProtectedRegion> {
        return worldGuardRegionContainer.get(guildPlotWorld)?.regions ?: mapOf()
    }

    // Create a region to claim around the new plot, the plot has uuid as its name
    suspend fun createRegion(uuid: UUID) { plugin.sync {
        val plot = plots.handler.getPlotById(uuid) ?: run {
            logger.error("Critical problem in creating a region, plot doesn't exist")
            return@sync
        }

        val plotWorld = plotConfig.getPlotWorld(plot.type)?.world() ?: run {
            logger.error("No such world exists")
            return@sync
        }

        val owner = plot.owner


        val min = BlockVector3.at(plot.claim.centre.blockX - plot.size.max, plotWorld.minHeight, plot.claim.centre.blockZ - plot.size.max)
        val max = BlockVector3.at(plot.claim.centre.blockX + plot.size.max - 1, plotWorld.maxHeight, plot.claim.centre.blockZ + plot.size.max - 1) // Define region size and location

        val domain = DefaultDomain()
        val protectedRegion: ProtectedCuboidRegion = when (owner) {
            is PlotOwner.GuildOwner -> {

                for (member in owner.guild.members ?: listOf()) { // Add all guild members to region domain
                    domain.addPlayer(member.uuid.toString())
                }

                ProtectedCuboidRegion(owner.guild.id.toString(), min, max) // Initiate the region
            }

            is PlotOwner.PlayerOwner -> {

                domain.addPlayer(owner.player.uniqueId.toString()) // Add player to domain of region

                ProtectedCuboidRegion(owner.player.uniqueId.toString(), min, max)
            }
        }

        protectedRegion.owners = domain // Update owners of region

        worldGuardPlatform.regionContainer.get(plotWorld.localWorld())?.addRegion(protectedRegion) // Add region to the world
    } }
}

object Locals {
    fun Player.localPlayer(): LocalPlayer {
        return WorldGuardPlugin.inst().wrapPlayer(this)
    }

    fun World.localWorld(): com.sk89q.worldedit.world.World {
        return BukkitAdapter.adapt(this)
    }

    fun String.localWorld(): com.sk89q.worldedit.world.World {
        return BukkitAdapter.adapt(Bukkit.getWorld(this))
    }

    fun Location.localLocation(): com.sk89q.worldedit.util.Location {
        return BukkitAdapter.adapt(this)
    }

    // Not a local
    fun String.world(): World? {
        return Bukkit.getWorld(this)
    }
}
