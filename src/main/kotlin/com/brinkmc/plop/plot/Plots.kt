package com.brinkmc.plop.plot

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.handler.PlotHandler
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.PlotKey
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.entity.Player
import java.util.UUID

class Plots(override val plugin: Plop): Addon, State {

    val plotList: MutableList<Plot> = mutableListOf()

    val plotMap: HashMap<PlotKey, Plot> = hashMapOf()

    // Sub handlers
    lateinit var plotHandler: PlotHandler

    override fun load() {
        plotHandler = PlotHandler(plugin)
    }

    override fun kill() {
        TODO("Not yet implemented")
    }

    fun getPlot(plotId: UUID): Plot? {
        return plotHandler.getPlotById(plotId)
    }

    // Extension functions
    fun Player.personalPlot(): Plot? {
        // Get a list of all the plots player owns. 1-to-1 relationship
        return plotHandler.getPlotByOwner(uniqueId)

    }

    fun Player.guild(): Guild {
        return Guild
    }

    fun Player.guildPlot(): Plot? {
        // One-liner to return only guild plots, which have the player as a member or the player as the leader
        return plotHandler.getPlotByGuild(player.guild().uuid)
    }
}