package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.modifier.PlotClaim
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisit
import com.brinkmc.plop.shared.hooks.Locals.localWorld
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

/*
I find best practice for data classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

enum class PlotType {
    PERSONAL,
    GUILD
}

data class Plot(
    val plotId: UUID, // Unique ID for the plot, could correlate to either Player UUID or Guild UUID depending on type below
    val type: PlotType,

    // Plot data
    var claim: PlotClaim,
    var visit: PlotVisit,
    var size: PlotSize,
    var factory: PlotFactory,
    var shop: PlotShop,
    var totem: PlotTotem
) {
    val owner: PlotOwner by lazy {
        if (type == PlotType.GUILD) {
            val guild = Guilds.getApi().getGuild(plotId) // Try to find guild

            if (guild != null) {
                PlotOwner.GuildOwner(guild)
            } else {
                throw IllegalStateException("Guild not found for plot $plotId")
            }
        } else {
            PlotOwner.PlayerOwner(Bukkit.getOfflinePlayer(plotId))
        }
    }
}
