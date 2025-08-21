package com.brinkmc.plop.shared.util

import com.brinkmc.plop.plot.preview.Direction
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import com.brinkmc.plop.shared.util.CoroutineUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

/*
Credit to Andross for the code, and HawkFalcon for commissioning it
 */

object ClaimUtils {

    private val HOLLOW_MATERIALS: Set<Material> = Material.entries.filter { it.isEmpty || !it.isSolid}.toSet()

    private fun isBlockUnsafe(location: Location): Boolean {
        return isBlockDamaging(location) || isBlockAboveAir(location)
    }

    private fun isBlockDamaging(location: Location): Boolean {
        val world = location.world
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ

        val below: Block = world.getBlockAt(x, y - 1, z)
        if (below.type == Material.LAVA || below.type == Material.FIRE) return true

        val block: Block = world.getBlockAt(x, y, z)
        if (block.type == Material.END_PORTAL || block.type == Material.NETHER_PORTAL) return true

        return (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).type)) || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).type))
    }

    private fun isBlockAboveAir(location: Location): Boolean {
        val world = location.world
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ

        return y > world.maxHeight || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).type)
    }

    fun getSafe(location: Location): Location {
        val world = location.world
        val maxDistance = 16
        var safe = false

        var length = 1
        var lengthCount = 0
        var direction = Direction.NORTH
        var initialLocation = location.block.location.clone().add(0.5, 0.0, 0.5)

        do {
            initialLocation = when (direction) { // Change logic depending on direction
                Direction.NORTH -> {
                    initialLocation.add(1.0, 0.0, 0.0)
                }

                Direction.EAST -> {
                    initialLocation.add(0.0, 0.0, 1.0)
                }

                Direction.SOUTH -> {
                    initialLocation.subtract(1.0, 0.0, 0.0)
                }

                Direction.WEST -> {
                    initialLocation.subtract(0.0, 0.0, 1.0)
                }
            }
            initialLocation.y = world.getHighestBlockAt(initialLocation, HeightMap.WORLD_SURFACE).y.toDouble() + 1.0

            lengthCount++ // Increment, it goes 1,1,2,2,3,3,4,4,5,5,6,6 which is handy can change every north and south

            if (lengthCount == length) {
                lengthCount = 0
                direction = direction.next()
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    length++
                }
            }

            for (i in (-2)..2) {
                val newLocation = initialLocation.clone().add(0.0, i.toDouble(), 0.0)
                if (!isBlockUnsafe(newLocation)) {
                    safe = true
                    initialLocation = newLocation
                    break
                }
            }
        } while (!safe && length < maxDistance)

         return initialLocation.add(0.0, 0.0, 0.0)
    }
}