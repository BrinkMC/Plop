package com.brinkmc.plop.plot.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.Direction
import com.brinkmc.plop.shared.constant.StringLocation
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.hook.api.Locals.world
import com.brinkmc.plop.plot.constant.PlotConstant
import com.brinkmc.plop.shared.util.type.LinkedList
import com.brinkmc.plop.shared.util.type.Node
import com.brinkmc.plop.plot.constant.PlotType
import org.bukkit.Location
import java.util.concurrent.atomic.AtomicBoolean

class PlotLayoutService(override val plugin: Plop) : State, Addon {

    val openPlots = mutableMapOf<PlotType, LinkedList<StringLocation>>() // x:y:z format to OpenPlot

    override suspend fun load() {
        for (plotType in PlotType.entries) {
            openPlots[plotType] = LinkedList()
            generateOpenPositions(plotType)
        }
    }

    override suspend fun kill() {
        openPlots.clear()
    }

    fun getCentrePlot(plotType: PlotType): Location {
        val maxPlotLength = configService.plotConfig.getPlotMaxSize(plotType).toDouble()
        val plotWorld = configService.plotConfig.getPlotWorld(plotType).world()
        val half: Double = (maxPlotLength/2)
        return Location(plotWorld, half, PlotConstant.LAYOUT_HEIGHT, half)
    }

    private suspend fun generateOpenPositions(plotType: PlotType) {
        openPlots[plotType]?.clear()

        // Constant values
        val maxPlotLength = configService.plotConfig.getPlotMaxSize(plotType).toDouble()
        val maxPreviewLimit = ((hookService.worldGuard.getPlotRegions(plotType).size * 3) + 100)
        // Tracking values
        var length = 1
        var lengthCount = 0
        var direction = Direction.NORTH
        var initialLocation = getCentrePlot(plotType).clone()

        do {
            initialLocation = when (direction) { // Change logic depending on direction
                Direction.NORTH -> {
                    initialLocation.add(
                        maxPlotLength,
                        0.0,
                        0.0
                    )
                }

                Direction.EAST -> {
                    initialLocation.add(
                        0.0,
                        0.0,
                        maxPlotLength
                    )
                }

                Direction.SOUTH -> {
                    initialLocation.subtract(
                        maxPlotLength,
                        0.0,
                        0.0
                    )
                }

                Direction.WEST -> {
                    initialLocation.subtract(
                        0.0,
                        0.0,
                        maxPlotLength
                    )
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

            val isNotClaimed = plugin.hookService.worldGuard.getRegions(initialLocation).isEmpty()

            if (isNotClaimed) {
                openPlots[plotType]?.append(
                    StringLocation(
                        initialLocation.world.name,
                        initialLocation.x,
                        initialLocation.y,
                        initialLocation.z,
                        AtomicBoolean(true)
                    )
                ) // Location isn't being looked at
            }

        } while (openPlots[plotType]!!.size < maxPreviewLimit)
    }

    suspend fun getFirstFree(plotType: PlotType): Node<StringLocation> { return plugin.asyncScope {
        var next = openPlots[plotType]?.first() // Get the first node
        while (next != null) { // Loop until the end of the list
            if (next.value.free) { // Check if the plot is free
                return@asyncScope next
            }
            next = next.next
        }
        throw RuntimeException("No open plots found")
    } }

    suspend fun getNextFreePlot(node: Node<StringLocation>, plotType: PlotType): Node<StringLocation> { return plugin.asyncScope {
        val origin = node
        var search = node.next
        while (search != origin) {
            if (search == null) {
                search = openPlots[plotType]?.first()
                continue
                // Start from the last node
            }

            if (search.value.free) {
                return@asyncScope search
            }

            search = search.next
        }
        throw RuntimeException("No open plots found")
    } }

    suspend fun getPreviousFreePlot(node: Node<StringLocation>, plotType: PlotType): Node<StringLocation> { return plugin.asyncScope {
        val origin = node
        var search = node.prev
        while (search != origin) {
            if (search == null) {
                search = openPlots[plotType]?.last()
                continue
                // Start from the last node
            }

            if (search.value.free) {
                return@asyncScope search
            }

            search = search.prev
        }
        throw RuntimeException("No open plots found")
    } }
}