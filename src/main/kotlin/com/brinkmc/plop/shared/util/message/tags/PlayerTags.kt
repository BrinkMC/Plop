package com.brinkmc.plop.shared.util.message.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

class PlayerTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    suspend fun all(player: Player): TagResolver {

        return TagResolver.resolver(
            name(player),
            wealth(player)
        )

    }

    private fun name(player: Player): TagResolver {
        return Placeholder.component(
            "name",
            player.displayName()
        )
    }

    private fun wealth(player: Player): TagResolver {
        return Placeholder.component(
            "wealth",
            miniMessage.deserialize(economy.getBalance(player).toString())
        )
    }
}
