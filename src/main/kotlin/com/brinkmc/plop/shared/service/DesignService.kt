package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.design.tags.PlayerTags
import com.brinkmc.plop.shared.design.tags.PlotTags
import com.brinkmc.plop.shared.design.tags.ShopTags
import com.brinkmc.plop.shop.dto.Shop
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

class DesignService(override val plugin: Plop): Addon {

    fun setSkull(itemStack: ItemStack, owner: PlotOwner?): ItemStack {
        val meta = itemStack.itemMeta as SkullMeta
        meta.playerProfile = owner?.getSkull()
        itemStack.itemMeta = meta
        return itemStack
    }

    suspend fun getItem(itemKey: ItemKey, name: MessageKey, description: MessageKey, playerId: UUID? = null, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver): ItemStack {
        return itemKey.item.name(name, playerId, shopId, plotId, *args).description(description, playerId, shopId, plotId, *args).clone()
    }

    suspend fun getItem(itemStack: ItemStack, name: MessageKey, description: MessageKey, playerId: UUID? = null, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver): ItemStack {
        return itemStack.name(name, playerId, shopId, plotId, *args).description(description, playerId, shopId, plotId, *args).clone()
    }

    suspend fun sendMiniMessage(playerId: UUID, message: MessageKey, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver) {
        val component = deserialise(message, playerId = playerId, shopId = shopId, plotId = plotId, args = args)

        playerService.sendMessage(playerId, component)
    }

    suspend fun sendMiniTitle(playerId: UUID, titleMessage: MessageKey, subTitleMessage: MessageKey, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver) {
        val componentOne = deserialise(titleMessage, playerId = playerId, shopId = shopId, plotId = plotId, args = args)
        val componentTwo = deserialise(subTitleMessage, playerId = playerId, shopId = shopId, plotId = plotId, args = args)

        playerService.showTitle(playerId, Title.title(componentOne, componentTwo))
    }

    suspend fun sendMiniActionBar(playerId: UUID, message: MessageKey, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver) {
        val component = deserialise(message, playerId = playerId, shopId = shopId, plotId = plotId, args = args)
        playerService.sendActionBar(playerId, component)
    }

    fun sendSound(playerId: UUID, key: SoundKey, volume: Float = 1.0f, pitch: Float = 1.0f) {
        playerService.playSound(playerId, Sound.sound(key.key, Sound.Source.MASTER, volume, pitch))
    }

    // Private

    private val miniMessage: MiniMessage = MiniMessage.builder()
        .tags(
            TagResolver.builder() // Define some of the tags
            .resolver(StandardTags.defaults())
            .build()
        )
        .build()

    private val playerTags = PlayerTags(plugin, miniMessage)
    private val shopTags = ShopTags(plugin, miniMessage)
    private val plotTags = PlotTags(plugin, miniMessage)

    private val plopMessageSource = plugin.plopMessageSource()

    // All possible iteration of deserialisation methods which are possible

    private suspend  fun deserialise(key: MessageKey, playerId: UUID?, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(key) ?: key.toString(),
            getTags(playerId, shopId, plotId),
            *args
        )
    }

    private suspend fun getTags(playerId: UUID?, shopId: UUID? = null, plotId: UUID? = null): TagResolver {
        return TagResolver.resolver(
            playerTags.all(playerId),
            shopTags.all(shopId, playerId),
            plotTags.all(plotId)
        )
    }

    private fun resolveTags(string: String, tags: TagResolver): String { // Resolve tags and then un-resolve LOL
        return miniMessage.serialize(miniMessage.deserialize(string, tags))
    }

    // Item stack
    private suspend fun ItemStack.name(name: MessageKey, playerId: UUID?, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(deserialise(name, playerId = playerId, shopId = shopId, plotId = plotId, args = args))
        }
        return this
    }

    private suspend fun ItemStack.description(description: MessageKey, playerId: UUID?, shopId: UUID? = null, plotId: UUID? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(deserialise(description, playerId = playerId, shopId = shopId, plotId = plotId, args = args)))
        }
        return this
    }
}