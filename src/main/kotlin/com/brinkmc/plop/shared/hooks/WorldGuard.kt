package com.brinkmc.plop.shared.hooks

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.UUID

class WorldGuard(override val plugin: Plop): Addon, State {

    lateinit var worldGuardAPI: WorldGuard
    val worldGuardPlatform: WorldGuardPlatform
        get() = worldGuardAPI.platform
    val worldGuardRegionContainer: RegionContainer
        get() = worldGuardPlatform.regionContainer

    fun localPlayer(player: Player): LocalPlayer {
        return WorldGuardPlugin.inst().wrapPlayer(player)
    }

    fun localWorld(world: World): com.sk89q.worldedit.world.World {
        return BukkitAdapter.adapt(world)
    }

    fun localLocation(location: Location): com.sk89q.worldedit.util.Location {
        return BukkitAdapter.adapt(location)
    }

    override suspend fun load() {
        worldGuardAPI = WorldGuard.getInstance()
    }

    override suspend fun kill() {

    }

    fun getRegion(world: String, uuid: UUID): ProtectedRegion? {
        val world = Bukkit.getWorld(world)

        if (world == null) {
            logger.error("Catastrophic error trying to get region")
            return null
        }

        return worldGuardRegionContainer.get(localWorld(world))?.regions?.get(uuid.toString())
    }

    fun createRegion(uuid: UUID) {
        val protectedRegion = ProtectedRegion()
    }
}