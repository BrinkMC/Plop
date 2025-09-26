package com.brinkmc.plop.shared.design.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.util.UUID

class PlotTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    fun all(plotId: UUID?): TagResolver {
        if (plotId == null) {
            return TagResolver.resolver()
        }

        val resolvers = listOfNotNull(
            nameTag(plotId),
            shopLevelTag(plotId)
        )

        return TagResolver.resolver(
            resolvers
        )

    }

    private suspend fun nameTag(plotId: UUID): TagResolver? {
        val plotOwnerName = plotService.getPlotOwnerDisplayName(plotId) ?: return null
        return Placeholder.component(
            "plot_owner",
            miniMessage.deserialize(plotOwnerName)
        )
    }

    private fun shopLevelTag(plotId: UUID): TagResolver? {
        val plotShopLevel = plotShopService.getShopLevel(plotId) ?: return null
        return Placeholder.component(
            "plot_shop_level",
            Component.text(plot.shop.level)
        )
    }
}
