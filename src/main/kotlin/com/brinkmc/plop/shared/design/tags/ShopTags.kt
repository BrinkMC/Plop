package com.brinkmc.plop.shared.design.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.dto.Shop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import java.util.UUID

class ShopTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    suspend fun all(shopId: UUID?, playerId: UUID?): TagResolver {
        if (shopId == null) {
            return TagResolver.resolver()
        }

        val resolvers = listOfNotNull(
            nameTag(shopId),
            itemTag(shopId),
            quantityTag(shopId),
            priceTag(shopId),
            if (playerId != null) totalTag(playerId) else null,
            if (playerId != null) multiplierTag(playerId) else null
        )
        return TagResolver.resolver(
            resolvers
        )
    }

    private suspend fun totalTag(playerId: UUID): TagResolver? {
        val multiplier = shopAccessService.getTotal(playerId) ?: return null
        return Placeholder.component(
            "total",
            Component.text(multiplier)
        )
    }

    private suspend fun multiplierTag(playerId: UUID): TagResolver? {
        val multiplier = shopAccessService.getMultiplier(playerId) ?: return null
        return Placeholder.component(
            "multiplier",
            Component.text(multiplier)
        )
    }

    private suspend fun nameTag(shopId: UUID): TagResolver? {
        val shopOwner = shopService.getShopOwnerDisplayName(shopId) ?: return null
        return Placeholder.component(
            "shop_owner",
            miniMessage.deserialize(shopOwner)
        )
    }

    private suspend fun itemTag(shopId: UUID): TagResolver? {
        val shopItemName = shopService.getShopItem(shopId)?.displayName() ?: return null
        return Placeholder.component(
            "shop_item",
            shopItemName
        )
    }

    private suspend fun quantityTag(shopId: UUID): TagResolver? {
        val shopQuantity = shopService.getShopQuantity(shopId) ?: return null
        return Placeholder.component(
            "shop_quantity",
            Component.text(shopQuantity)
        )
    }

    private suspend fun priceTag(shopId: UUID): TagResolver? {
        val shopPrice = shopService.getShopPrice(shopId) ?: return null
        return Placeholder.component(
            "shop_price",
            Component.text(shopPrice)
        )
    }
}