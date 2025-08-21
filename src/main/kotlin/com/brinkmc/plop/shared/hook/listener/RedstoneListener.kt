package com.brinkmc.plop.shared.hook.listener

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockRedstoneEvent

class RedstoneListener(override val plugin: Plop): Addon, State, Listener {

    override suspend fun load() {}

    override suspend fun kill() {}

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun redstoneTrigger(event: BlockRedstoneEvent) {
        val plot = plotHandler.getCurrentPlot(event.block.location)

        if (plot == null) {
            return // No plot, no redstone, thus I do not care
        }

        if (plots.redstoneHandler .isRedstoneEnabled()) {
            plot.redstoneHandler.triggerRedstone(event.block, event.newCurrent)
        } else {
            event.isCancelled = true // Cancel the redstone event if redstone is disabled
        }
    }
}