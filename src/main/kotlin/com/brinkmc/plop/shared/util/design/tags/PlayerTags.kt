package com.brinkmc.plop.shared.util.design.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

class PlayerTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    fun all(player: Player?): TagResolver {
        if (player == null) {
            return TagResolver.resolver()
        }

        return TagResolver.resolver(
            nameTag(player),
            wealthTag(player)
        )

    }

    private fun nameTag(player: Player): TagResolver {
        return Placeholder.component(
            "name",
            player.displayName()
        )
    }

    private fun wealthTag(player: Player): TagResolver {
        return Placeholder.component(
            "wealth",
            miniMessage.deserialize(economy.getBalance(player).toString())
        )
    }
}
