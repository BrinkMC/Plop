package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player

class MessageService(override val plugin: Plop): Addon {

    private val profileTags = ProfileTags(plugin)
    private var miniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.builder() // Define some of the tags
            .resolver(StandardTags.defaults())
            .build()
        )
        .build()

    val plopMessageSource = plugin.plopMessageSource()

    fun decode(string: String): Component {
        return miniMessage.deserialize(string)
    }

    fun get(string: String, player: Player? = null, vararg args: Any): Component {
        return if (player == null) {
            miniMessage.deserialize(dealWithArgs(plopMessageSource.findMessage(string) ?: "<red>No message set for $string", args))
        } else {
            miniMessage.deserialize(dealWithArgs(plopMessageSource.findMessage(string) ?: "<red>No message set for $string", args), profileTags.name(player))
        }
    }

    fun dealWithArgs(string: String, vararg args: Any): String {
        var stringBuilder = string
        for (i in 0..(args.size-1)) {
            stringBuilder = stringBuilder.replace("%$i", args[i].toString())
        }
        return stringBuilder
    }

    // Provide functionality to the Addon reference
    fun sendFormattedMessageStr(player: Player, message: String) {
        player.sendMessage(
            miniMessage.deserialize(message, profileTags.name(player))
        )
    }

    fun sendFormattedMessageComp(player: Player, message: Component) {
        player.sendMessage(
            message
        )
    }
}

class ProfileTags(override val plugin: Plop) : Addon {

    fun all(player: Player): TagResolver {

        return TagResolver.resolver(
            name(player),
//            personalPlotResolver(player),
//            guildPlotResolver(player)
        )

    }

    fun name(player: Player): TagResolver {
        return Placeholder.component("name", player.displayName())
    }
}



