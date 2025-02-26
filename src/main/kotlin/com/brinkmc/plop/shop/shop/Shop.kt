package com.brinkmc.plop.shop.shop

import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.plot.plot.base.PlotType
import me.glaremasters.guilds.Guilds
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import java.util.UUID

enum class ShopType { // Types of shop
    BUY,
    SELL
}

data class Shop (
    // Primary
    val shopId: UUID,

    val plotId: UUID, // Plot
    val plotType: PlotType,
    val location: Location, // Location in plot


    // Mutable
    var shopType: ShopType,
    var ware: Item,
    var stock: Int,

    // Modifiable
    var price: Float

) {
    val owner: PlotOwner by lazy {
        if (plotType == PlotType.GUILD) {
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