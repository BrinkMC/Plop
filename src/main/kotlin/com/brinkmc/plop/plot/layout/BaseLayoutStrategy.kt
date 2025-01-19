package com.brinkmc.plop.plot.layout

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.preview.Direction
import com.brinkmc.plop.plot.preview.StringLocation
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.PlotConfig
import com.brinkmc.plop.shared.util.collection.LinkedList
import com.brinkmc.plop.shared.util.collection.Node
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseLayoutStrategy(override val plugin: Plop) : State, Addon {

    protected abstract val maxPlotLength: Double
    protected abstract val maxPreviewLimit: Int
    protected abstract val worldName: String
    protected abstract val worldGen: String

    val openPlots = LinkedList<StringLocation>() // x:y:z format to OpenPlot

    lateinit var world: World
    lateinit var centrePlot: Location

    override suspend fun load() {
        // Create world if it doesn't exist already
        if (plugin.server.getWorld(worldName) == null) {
            plugin.server.createWorld(WorldCreator.name(worldName).generator(worldGen))
        }

        world = plugin.server.getWorld(worldName)!!
        // Now that we can set a world which isn't null

        val half: Double = (maxPlotLength / 2)
        centrePlot = Location(world, half, 100.0, half) // Set centre plot

        generateOpenPositions()
    }

    override suspend fun kill() {
        plotConfig.kill()
        openPlots.clear()
    }

    private fun generateOpenPositions() {
        openPlots.clear()

        val avoidList = hashSetOf<StringLocation>() // List of pre-existing plots

        for (plot in plugin.plots.plotMap.values) {
            avoidList.add(
                StringLocation(
                    plot.claim.world,
                    plot.claim.centre.x,
                    plot.claim.centre.y,
                    plot.claim.centre.z
                )
            ) // Centre should be in format x, 100, z
        }

        // Tracking values
        var length = 1
        var lengthCount = 0
        var direction = Direction.NORTH
        var initialLocation = centrePlot.clone()

        do {
            initialLocation = when (direction) { // Change logic depending on direction
                Direction.NORTH -> {
                    initialLocation.add(maxPlotLength, 0.0, 0.0)
                }

                Direction.EAST -> {
                    initialLocation.add(0.0, 0.0, maxPlotLength)
                }

                Direction.SOUTH -> {
                    initialLocation.subtract(maxPlotLength, 0.0, 0.0)
                }

                Direction.WEST -> {
                    initialLocation.subtract(0.0, 0.0, maxPlotLength)
                }
            }

            lengthCount++ // Increment, it goes 1,1,2,2,3,3,4,4,5,5,6,6 which is handy can change every north and south

            if (lengthCount == length) {
                lengthCount = 0
                direction = direction.next()
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    length++
                }
            }

            val isClaimed = avoidList.contains(StringLocation(initialLocation.world.name,initialLocation.x, initialLocation.y, initialLocation.z))

            if (!isClaimed) {
                openPlots.append(
                    StringLocation(
                        initialLocation.world.name,
                        initialLocation.x,
                        initialLocation.y,
                        initialLocation.z,
                        AtomicBoolean(false)
                    )
                ) // Location isn't being looked at
            }

        } while (openPlots.size < maxPreviewLimit)
    }

    fun getFirstFree(): Node<StringLocation>? {
        var next = openPlots.first() // Get the first node
        while (next != null && next.value.open == false) { // Loop while no free plots
            next = next.next

            if (next == null) {
                logger.error("No plot available at all?")
                return null
            }

        }
        return next
    }

    fun getNextFreePlot(node: Node<StringLocation>): Node<StringLocation>? {
        var looked = false
        var next = node.next // Next node
        while (next != null && !next.value.open) { // Loop while no free plots
            next = next.next

            if (next == null && looked) {
                logger.error("No plot available forwards?? Bizarre")
                return null
            }

            if (next == null) {
                next = openPlots.first()
                looked = true
            }

        }
        return next
    }

    fun getPreviousFreePlot(node: Node<StringLocation>): Node<StringLocation>? {
        var looked = false
        var previous = node.prev // Previous node
        while (previous != null && !previous.value.open) { // Loop while no free plots
            previous = previous.next

            if (previous == null && looked) {
                logger.error("No plot available backwards?? Bizarre")
                return null
            }

            if (previous == null) {
                previous = openPlots.last()
                looked = true
            }

        }
        return previous
    }
}