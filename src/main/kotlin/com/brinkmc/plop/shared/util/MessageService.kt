package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player
import sun.net.www.content.text.plain

class MessageService(override val plugin: Plop): Addon {

    private val profileTags = ProfileTags(plugin)
    private var miniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.builder() // Define some of the tags
            .resolver(StandardTags.defaults())
            .build()
        )
        .build()

    val plopMessageSource = plugin.plopMessageSource()

    fun get(string: String, player: Player? = null): Component {
        return if (player == null) {
            miniMessage.deserialize(plopMessageSource.findMessage(string) ?: "<red>No message set!")
        } else {
            miniMessage.deserialize(plopMessageSource.findMessage(string) ?: "<red>No message set!", profileTags.name(player))
        }
    }

    // Provide functionality to the Addon reference
    fun sendFormattedMessage(player: Player, message: String) {
        player.sendMessage(
            miniMessage.deserialize(message, profileTags.name(player))
        )
    }

    fun sendFormattedMessage(player: Player, message: Component) {
        player.sendMessage(
            message
        )
    }
}

class ProfileTags(override val plugin: Plop) : Addon {

    fun all(player: Player): TagResolver {

        return TagResolver.resolver(
            name(player),
            personalPlotResolver(player),
            guildPlotResolver(player)
        )

    }

    fun name(player: Player): TagResolver {
        return Placeholder.component("name", player.displayName())
    }

    fun personalPlotResolver(player: Player): TagResolver {
        return Placeholder.component("guildPlot", Component.text(plugin.hooks.guilds.guildAPI.getGuildByPlayerId(player.uniqueId)?.name ?: ""))
    }

    fun guildPlotResolver(player: Player): TagResolver {
        return Placeholder.component("guildPlot", Component.text(plugin.hooks.guilds.guildAPI.getGuildByPlayerId(player.uniqueId)?.name ?: ""))
    }
}



