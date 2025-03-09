package com.brinkmc.plop.shared.util.message

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.message.tags.PlayerTags
import com.brinkmc.plop.shared.util.message.tags.PlotTags
import com.brinkmc.plop.shared.util.message.tags.ShopTags
import com.brinkmc.plop.shop.shop.Shop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player

class MessageService(override val plugin: Plop): Addon {

    private val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.builder() // Define some of the tags
            .resolver(StandardTags.defaults())
            .build()
        )
        .build()

    private val playerTags = PlayerTags(plugin, miniMessage)
    private val shopTags = ShopTags(plugin, miniMessage)
    private val plotTags = PlotTags(plugin, miniMessage)

    private val plopMessageSource = plugin.plopMessageSource()

    // All possible iteration of deserialisation methods which are possible

    fun deserialise(key: MessageKey, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(key) ?: key.toString(),
            getTags(player, shop, plot),
            *args
        )
    }

    fun getTags(player: Player? = null, shop: Shop? = null, plot: Plot? = null): TagResolver {
        return TagResolver.resolver(
            playerTags.all(player),
            shopTags.all(shop),
            plotTags.all(plot)
        )
    }

    fun resolveTags(string: String, tags: TagResolver): String { // Resolve tags and then un-resolve LOL
        return miniMessage.serialize(miniMessage.deserialize(string, tags))
    }
}





