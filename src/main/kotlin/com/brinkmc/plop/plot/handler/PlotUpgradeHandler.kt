package com.brinkmc.plop.plot.handler

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.util.message.MessageKey
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

        if (!plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.sizeHandler.upgradePlot(plot) // Update the plot in the database
    }

    fun upgradeVisitorLevel(plot: Plot, initiator: Player) {
        logger.info("Upgrading plot visitor level")

        if (plot.visit.level == plots.visitorHandler.getHighestLevel(plot.type)) return // Already at max level

        // Calculate if they can afford it
        val potentialLevel = plots.visitorHandler.getLevel(plot.type, plot.size.level + 1)

        // Using the economy API, check if they can afford it
        if (!plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.visitorHandler.upgradePlot(plot) // Update the plot in the database
    }

    fun upgradeShopLevel(plot: Plot, initiator: Player) {
        logger.info("Upgrading shop limit level")

        if (plot.shop.level == plots.shopHandler.getHighestLevel(plot.type)) return // Already at max level

        // Calculate if they can afford it
        val potentialLevel = plots.shopHandler.getLevel(plot.type, plot.shop.level + 1)

        // Using the economy API, check if they can afford it

        if (!plot.owner.hasBalance(economy, potentialLevel?.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.shopHandler.upgradePlot(plot) // Update the plot in the database
    }

    fun upgradeFactoryLevel(plot: Plot, initiator: Player) {
        logger.info("Upgrading factory limit level")

        if (plot.factory.level == plots.factoryHandler.getHighestLevel(plot.type)) return // Already at max level

        // Calculate if they can afford it
        val potentialLevel = plots.factoryHandler.getLevel(plot.type, plot.factory.level + 1)

        // Using the economy API, check if they can afford it

        if (!plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.factoryHandler.upgradePlot(plot) // Update the plot in the database
    }

    fun upgradeTotemLevel(plot: Plot, initiator: Player) {
        logger.info("Upgrading totem level")

        if (plot.totem.level == plots.totemHandler.getHighestLevel(plot.type)) return // Already at max level

        // Calculate if they can afford it
        val potentialLevel = plots.totemHandler.getLevel(plot.type, plot.totem.level + 1)

        // Using the economy API, check if they can afford it

        if (!plot.owner.hasBalance(economy, potentialLevel.price?.toDouble() ?: 0.0)) {
            initiator.sendMiniMessage(MessageKey.NO_MONEY)
            return
        }

        plots.totemHandler.upgradePlot(plot) // Update the plot in the database
    }
}