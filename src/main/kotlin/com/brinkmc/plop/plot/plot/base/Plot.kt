package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.data.PlotClaim
import com.brinkmc.plop.plot.plot.data.PlotVisitState
import com.brinkmc.plop.plot.plot.modifier.PlotFactory
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.PlotShop
import com.brinkmc.plop.plot.plot.modifier.PlotTotem
import com.brinkmc.plop.plot.plot.modifier.PlotVisitLimit
import com.brinkmc.plop.shared.hooks.Locals.localWorld
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
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
    val plotId: UUID, // Unique ID for the plot
    val type: PlotType,

    // Primary
    val ownerId: UUID, // It may be a Player UUID OR Guild UUID

    // Plot data
    val claim: PlotClaim,
    val visitState: PlotVisitState,
    var visitLimit: PlotVisitLimit,
    var size: PlotSize,
    var factory: PlotFactory,
    var shop: PlotShop,
    var totem: PlotTotem
) {
    val owner: PlotOwner by lazy {
        if (type == PlotType.GUILD) {
            PlotOwner.GuildOwner(Guilds.getApi().getGuild(ownerId))
        }
        else {
            PlotOwner.PlayerOwner(Bukkit.getOfflinePlayer(ownerId))
        }
    }

    fun getRegion(worldGuard: WorldGuard): ProtectedCuboidRegion {
        return worldGuard.platform.regionContainer.get(this.claim.world.localWorld())?.regions?.get(this.plotId.toString()) as ProtectedCuboidRegion
    }
}
