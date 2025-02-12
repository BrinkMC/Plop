package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.block.Vault
import org.bukkit.entity.Player

class PlotUpgradeHandler(override val plugin: Plop): Addon, State {
    override suspend fun load() {
        logger.info("Loaded PlotUpgradeHandler")

    }

    override suspend fun kill() { }


    suspend fun upgradeSizeLevel(plot: Plot, initiator: Player) { // Validate that they can afford price of new level
        logger.info("Upgrading plot claim")

        if (plot.size.level == plots.sizeHandler.getHighestLevel(plot.type)) return // Already at max level

        // Plots start with level 1 (position 0 in list)


        // Calculate if they can afford it
        val potentialLevel =  plots.sizeHandler.getLevel(plot.type, plot.size.level + 1)

        // Using the economy API, check if they can afford it

        if (plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendFormattedMessage(lang.get("not-enough-money"))
            return
        }

        plots.sizeHandler.upgradePlot(plot) // Update the plot in the database
    }

    fun upgradeVisitorLevel(plot: Plot) {
        logger.info("Upgrading plot visitor level")

        if (plot.visit.level == plots.visitorHandler.getHighestLevel(plot.type)) return // Already at max level

        // Calculate if they can afford it
        val potentialLevel = plots.visitorHandler.getLevel(plot.type, plot.size.level + 1)

        // Using the economy API, check if they can afford it
        if (plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            (plot.owner as PlotOwner.PlayerOwner).onlinePlayer()?.sendFormattedMessage(lang.get("not-enough-money"))
            return
        }
    }

}