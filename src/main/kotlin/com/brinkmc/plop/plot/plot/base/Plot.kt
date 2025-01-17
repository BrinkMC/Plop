package com.brinkmc.plop.plot.plot.base

import com.brinkmc.plop.plot.plot.data.Claim
import com.brinkmc.plop.plot.plot.data.PlotVisit
import com.brinkmc.plop.plot.plot.modifier.FactoryLimit
import com.brinkmc.plop.plot.plot.modifier.PlotSize
import com.brinkmc.plop.plot.plot.modifier.ShopLimit
import com.brinkmc.plop.plot.plot.modifier.VisitorLimit
import com.brinkmc.plop.plot.plot.structure.Totem
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.GuildsAPI
import me.glaremasters.guilds.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/*
I find best practice for data classes tends to be separate the logic into extension functions.
This works well in keeping the data class itself relatively clean
 */

enum class PLOT_TYPE {
    Personal,
    Guild
}

data class Plot(
    val plotId: UUID, // Unique ID for the plot
    val type: PLOT_TYPE,

    // Primary
    val ownerId: UUID, // It may be a Player UUID OR Guild UUID

    //Inherited from Plot interface
    val claim: Claim,
    var visitorLimit: VisitorLimit,
    var plotSize: PlotSize,
    var factoryLimit: FactoryLimit,
    var shopLimit: ShopLimit,

    // Might not exist in database
    val totems: MutableList<Totem>,
    val plotVisit: PlotVisit,
) {
    fun getOwner(): PlotOwner {
        return if (type == PLOT_TYPE.Guild) {
            PlotOwner.GuildOwner(Guilds.getApi().getGuild(ownerId))
        }
        else {
            PlotOwner.PlayerOwner(Bukkit.getOfflinePlayer(ownerId))
        }
    }
}
