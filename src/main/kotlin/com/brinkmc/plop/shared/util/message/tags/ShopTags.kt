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

    fun all(shop: Shop): TagResolver {

        return TagResolver.resolver(
            name(shop),
            selling(shop),
            buying(shop),
            quantity(shop),
            price(shop)
        )

    }

    private fun name(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_owner",
            miniMessage.deserialize(shop.owner.name)
        )
    }

    private fun selling(shop: Shop): TagResolver {
        return Placeholder.component(
            "shop_selling",
            Component.text(shop.ware.name)
        )
    }
}