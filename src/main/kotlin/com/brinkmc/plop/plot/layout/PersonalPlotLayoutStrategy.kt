package com.brinkmc.plop.plot.layout

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.preview.Direction
import com.brinkmc.plop.plot.preview.StringLocation
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.config.configs.PlotConfig
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.WorldCreator

class PersonalPlotLayoutStrategy(override val plugin: Plop): State, Addon {

    val personalPlotConfig = PlotConfig(plugin)

    val openPlots = hashMapOf<StringLocation, Boolean>() // x:y:z format to OpenPlot

    lateinit var world: World
    lateinit var centrePlot: Location
    val maxPlotLength: Double = personalPlotConfig.personalPlotMaxSize.toDouble()

    var maxPreviewLimit: Int = (plots.plotHandler.plotMap.size * 3) + 100 // Amount of plots visitable before it says no more


    override fun load() {
        // Create world if it doesn't exist already
        if (server.getWorld(personalPlotConfig.personalPlotWorld) == null) {
            server.createWorld(WorldCreator.name(personalPlotConfig.personalPlotWorld).generator(personalPlotConfig.personalPlotWorldGenerator))
        }

        world = server.getWorld(personalPlotConfig.personalPlotWorld)!!
        // Now that we can set a world which isn't null

        val half: Double = (personalPlotConfig.personalPlotMaxSize / 2).toDouble()
        centrePlot =  Location(world, half, 100.0, half) // Set centre plot

        generateOpenPositions()
    }

    override fun kill() {
        personalPlotConfig.kill()
        openPlots.clear()
    }

    private fun generateOpenPositions() {
        val avoidList = hashSetOf<StringLocation>() // List of pre-existing plots

        for (plot in plots.plotMap.values) {
            avoidList.add(StringLocation(plot.claim.centre.x, plot.claim.centre.y, plot.claim.centre.z)) // Centre should be in format x, 100, z
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

            val isClaimed = avoidList.contains(StringLocation(initialLocation.x, initialLocation.y, initialLocation.z))

            if (!isClaimed) {
                openPlots[StringLocation(initialLocation.x, initialLocation.y, initialLocation.z)] = false // Location isn't being looked at
            }


        } while (openPlots.size < maxPreviewLimit)
    }


}