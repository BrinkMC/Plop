package com.brinkmc.plop.shared.util.message.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shop.shop.Shop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class ShopTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    fun all(shop: Shop?): TagResolver {
        if (shop == null) {
            return TagResolver.resolver()
        }

        return TagResolver.resolver(
            nameTag(shop),
            sellingTag(shop),
            buyingTag(shop),
            quantityTag(shop),
            priceTag(shop)
        )

    }

    private fun nameTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_owner",
            miniMessage.deserialize(shop.owner.getName())
        )
    }

    private fun sellingTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_selling",
            Component.text(shop.ware.name)
        )
    }

    private fun buyingTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_buying",
            Component.text(shop.ware.name)
        )
    }

    private fun quantityTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_quantity",
            Component.text(shop.stock)
        )
    }

    private fun priceTag(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_price",
            Component.text(shop.price)
        )
    }
}