package com.brinkmc.plop.shared.util.message

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.bukkit.entity.Player

class MessageService(override val plugin: Plop): Addon {

    private val profileTags = ProfileTags(plugin)
    val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(TagResolver.builder() // Define some of the tags
            .resolver(StandardTags.defaults())
            .build()
        )
        .build()

    private val plopMessageSource = plugin.plopMessageSource()

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

    fun decode(string: String): Component {
        return miniMessage.deserialize(string)
    }

    fun get(string: String, player: Player? = null): Component {
        return if (player == null) {
            miniMessage.deserialize(
                plopMessageSource.findMessage(string) ?: "<red>No message set for $string"
            )
        } else {
            miniMessage.deserialize(
                plopMessageSource.findMessage(string) ?: "<red>No message set for $string",
                profileTags.name(player)
            )
        }
    }
}

class ProfileTags(override val plugin: Plop) : Addon {

    val miniMessage = plugin.getMessageService().miniMessage

    suspend fun all(player: Player): TagResolver {

        return TagResolver.resolver(
            name(player),
            personalPlotResolver(player),
            guildPlotResolver(player)
        )

    }

    private fun name(player: Player): TagResolver {
        return Placeholder.component("name", player.displayName())
    }

    private suspend fun personalPlotResolver(player: Player): TagResolver {
        val personalPlot = player.personalPlot()
        return Placeholder.component(
            "p_plot_owner",
            miniMessage.deserialize(personalPlot?.owner?.getName() ?: "No Personal Plot")
        )
    }

    private suspend fun guildPlotResolver(player: Player): TagResolver {
        val guild = player.guild()
        val guildPlot = guild?.plot()
        return Placeholder.component(
            "g_plot_owner",
            miniMessage.deserialize(guildPlot?.owner?.getName() ?: "No Guild Plot")
        )
    }
}



