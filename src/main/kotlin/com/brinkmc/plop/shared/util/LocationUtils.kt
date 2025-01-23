package com.brinkmc.plop.shared.util

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block

/*
Credit to Andross for the code, and HawkFalcon for commissioning it
 */

object LocationUtils {

    private val HOLLOW_MATERIALS: Set<Material> = Material.values().filter { it.isEmpty }.toSet()

    private fun isBlockUnsafe(world: World, x: Int, y: Int, z: Int): Boolean {
        return isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z)
    }

    private fun isBlockDamaging(world: World, x: Int, y: Int, z: Int): Boolean {
        val below: Block = world.getBlockAt(x, y - 1, z)
        if (below.type == Material.LAVA || below.type == Material.FIRE) return true

        val block: Block = world.getBlockAt(x, y, z)
        if (block.type == Material.END_PORTAL || block.type == Material.NETHER_PORTAL) return true

        return (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).type)) || (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).type))
    }

    private fun isBlockAboveAir(world: World, x: Int, y: Int, z: Int): Boolean {
        return y > world.maxHeight || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).type)
    }

    private const val RADIUS = 3

    private val VOLUME: Array<Vector3D>

    data class Vector3D(val x: Int, val y: Int, val z: Int)

    init {
        val pos = mutableListOf<Vector3D>()
        for (x in -RADIUS..RADIUS) {
            for (y in -RADIUS..RADIUS) {
                for (z in -RADIUS..RADIUS) {
                    pos.add(Vector3D(x, y, z))
                }
            }
        }
        pos.sortBy { it.x * it.x + it.y * it.y + it.z * it.z }
        VOLUME = pos.toTypedArray()
    }

    @JvmStatic
    fun Location.getSafeDestination(): Location? {
        val world: World = this.world ?: return null
        var x = this.blockX
        var y = this.y.toInt()
        var z = this.blockZ
        val origX = x
        val origY = y
        val origZ = z
        while (isBlockAboveAir(world, x, y, z)) {
            y -= 1
            if (y < 0) {
                y = origY
                break
            }
        }
        if (isBlockUnsafe(world, x, y, z)) {
            x = if (this.x.toInt() == origX) x - 1 else x + 1
            z = if (this.z.toInt() == origZ) z - 1 else z + 1
        }
        var i = 0
        while (isBlockUnsafe(world, x, y, z)) {
            i++
            if (i >= VOLUME.size) {
                x = origX
                y = origY + RADIUS
                z = origZ
                break
            }
            x = origX + VOLUME[i].x
            y = origY + VOLUME[i].y
            z = origZ + VOLUME[i].z
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y += 1
            if (y >= world.maxHeight) {
                x += 1
                break
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y -= 1
            if (y <= 1) {
                x += 1
                y = world.getHighestBlockYAt(x, z)
                if (x - 48 > this.blockX) {
                    return null
                }
            }
        }
        return Location(world, x + 0.5, y.toDouble(), z + 0.5, this.yaw, this.pitch)
    }
}