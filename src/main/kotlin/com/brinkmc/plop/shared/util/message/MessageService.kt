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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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

    fun deserialise(string: String): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string
        )
    }

    suspend fun deserialise(string: String, player: Player): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            playerTags.all(player) // Set the tags
        )
    }

    fun deserialise(string: String, shop: Shop): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            shopTags.all(shop) // Set the tags
        )
    }

    fun deserialise(string: String, plot: Plot): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            plotTags.all(plot) // Set the tags
        )
    }

    suspend fun deserialise(string: String, player: Player, shop: Shop): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            playerTags.all(player),
            shopTags.all(shop) // Set the tags
        )
    }

    suspend fun deserialise(string: String, player: Player, plot: Plot): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            playerTags.all(player),
            plotTags.all(plot) // Set the tags
        )
    }

    fun deserialise(string: String, shop: Shop, plot: Plot): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            shopTags.all(shop),
            plotTags.all(plot) // Set the tags
        )
    }

    suspend fun deserialise(string: String, player: Player, shop: Shop, plot: Plot): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(string) ?: string,
            playerTags.all(player),
            shopTags.all(shop),
            plotTags.all(plot) // Set the tags
        )
    }
}





