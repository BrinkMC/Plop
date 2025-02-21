package com.brinkmc.plop.plot.layout

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotType
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

abstract class BaseLayoutStrategy(override val plugin: Plop, open val plotType: PlotType) : State, Addon {

    protected abstract val maxPlotLength: Double
    protected abstract val maxPreviewLimit: Int
    protected abstract val worldName: String
    protected abstract val worldGen: String

    val openPlots = LinkedList<StringLocation>() // x:y:z format to OpenPlot

    lateinit var world: World
    lateinit var centrePlot: Location

    override suspend fun load() {
        world = plugin.server.getWorld(worldName)!!
        // Now that we can set a world which isn't null

        val half: Double = (maxPlotLength / 2)
        centrePlot = Location(world, half, 100.0, half) // Set centre plot
        logger.info("Generating open positions in world $worldName")
        generateOpenPositions()
    }

    override suspend fun kill() {
        openPlots.clear()
    }

    private fun generateOpenPositions() {
        openPlots.clear()
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

            val isNotClaimed = (plugin.hooks.worldGuard.getRegions(initialLocation)?.size == 0)

            if (isNotClaimed) {
                openPlots.append(
                    StringLocation(
                        initialLocation.world.name,
                        initialLocation.x,
                        initialLocation.y,
                        initialLocation.z,
                        AtomicBoolean(true)
                    )
                ) // Location isn't being looked at
            }

        } while (openPlots.size < maxPreviewLimit)
    }

    suspend fun getFirstFree(): Node<StringLocation>? { return asyncScope {
        var next = openPlots.first() // Get the first node
        while (next != null) { // Loop until the end of the list
            if (next.value.free) { // Check if the plot is free
                return@asyncScope next
            }
            next = next.next
        }
        logger.error("No plot available at all?")
        return@asyncScope null
    } }

    suspend fun getNextFreePlot(node: Node<StringLocation>): Node<StringLocation>? { return asyncScope {
        val origin = node
        var search = node.next
        while (search != origin) {
            if (search == null) {
                search = openPlots.first()
                continue
                // Start from the last node
            }

            if (search.value.free) {
                return@asyncScope search
            }

            search = search.next
        }
        logger.error("No plot available backwards?? Bizarre")
        return@asyncScope null
    } }

    suspend fun getPreviousFreePlot(node: Node<StringLocation>): Node<StringLocation>? { return asyncScope {
        val origin = node
        var search = node.prev
        while (search != origin) {
            if (search == null) {
                search = openPlots.last()
                continue
                // Start from the last node
            }

            if (search.value.free) {
                return@asyncScope search
            }

            search = search.prev
        }
        logger.error("No plot available backwards?? Bizarre")
        return@asyncScope null
    } }
}