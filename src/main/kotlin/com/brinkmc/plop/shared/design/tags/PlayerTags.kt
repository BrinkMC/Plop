package com.brinkmc.plop.shared.design.tags

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import java.util.UUID

class PlayerTags(override val plugin: Plop, val miniMessage: MiniMessage) : Addon {

    fun all(playerId: UUID?): TagResolver {
        if (playerId == null) {
            return TagResolver.resolver()
        }

        val resolvers = listOfNotNull(nameTag(playerId), wealthTag(playerId))

        return TagResolver.resolver(
            resolvers
        )
    }

    private fun nameTag(playerId: UUID): TagResolver? {
        val displayName = playerService.getDisplayName(playerId) ?: return null
        return Placeholder.component(
            "name",
            displayName
        )
    }

    private fun wealthTag(playerId: UUID): TagResolver? {
        val wealth = economyService.getBalance(playerId) ?: return null
        return Placeholder.component(
            "wealth",
            miniMessage.deserialize(wealth.toString())
        )
    }
}
