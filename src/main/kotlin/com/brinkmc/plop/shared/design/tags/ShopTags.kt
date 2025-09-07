package com.brinkmc.plop.shared.design.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.dto.Shop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

class ShopTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    suspend fun all(shop: Shop?, player: Player?): TagResolver {
        if (shop == null) {
            return TagResolver.resolver()
        }

        if (player == null) {
            return TagResolver.resolver(
                nameTag(shop),
                itemTag(shop),
                quantityTag(shop),
                priceTag(shop),
            )
        }

        return TagResolver.resolver(
            nameTag(shop),
            itemTag(shop),
            quantityTag(shop),
            priceTag(shop),
            totalTag(player),
            multiplierTag(player)
        )

    }

    private suspend fun totalTag(player: Player): TagResolver {
        val multiplier = shopAccessService.getTotal(player.uniqueId) ?: 0
        return Placeholder.component(
            "total",
            Component.text(multiplier)
        )
    }

    private suspend fun multiplierTag(player: Player): TagResolver {
        val multiplier = shopAccessService.getMultiplier(player.uniqueId) ?: 1
        return Placeholder.component(
            "multiplier",
            Component.text(multiplier)
        )
    }

    private suspend fun nameTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_owner",
            miniMessage.deserialize(plotService.getPlotFromLocation(shop.location)?.owner?.getName() ?: "")
        )
    }

    private fun itemTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_item",
            shop.item.displayName()
        )
    }

    private fun quantityTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_quantity",
            Component.text(shop.quantity)
        )
    }

    private fun priceTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_price",
            Component.text(shop.price)
        )
    }
}