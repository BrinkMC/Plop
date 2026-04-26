package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.constant.PlotType
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.ServiceResult
import com.brinkmc.plop.shared.constant.SoundKey
import com.brinkmc.plop.shared.constant.Translatable
import com.brinkmc.plop.shared.design.tags.PlayerTags
import com.brinkmc.plop.shared.design.tags.PlotTags
import com.brinkmc.plop.shared.design.tags.ShopTags
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.title.Title
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.UUID

class DesignService(override val plugin: Plop): Addon {

    suspend fun setSkull(itemStack: ItemStack, skullOwner: UUID): ItemStack {
        // Handle Skull
        val meta = itemStack.itemMeta as SkullMeta

        when (plotService.getPlotType(skullOwner)) {
            PlotType.PERSONAL -> {
                playerService.getSkull(skullOwner)
                meta.playerProfile = playerService.getSkull(skullOwner)
            }
            PlotType.GUILD -> {
                val guild = hookService.guilds.getGuild(skullOwner)
                val guildSkullMeta = guild?.guildSkull?.itemStack?.itemMeta as SkullMeta
                meta.playerProfile = guildSkullMeta.playerProfile
            }
            else -> return itemStack
        }
        return itemStack
    }

    suspend fun getItem(itemKey: ItemKey, name: Translatable, description: Translatable, playerId: UUID? = null, shopId: UUID? = null, plotId: UUID? = null, factoryId: UUID? = null): ItemStack {
        return getItem(itemKey.item, name, description, playerId, shopId, plotId, factoryId)
    }

    suspend fun getItem(itemStack: ItemStack, name: Translatable, description: Translatable, playerId: UUID? = null, shopId: UUID? = null, plotId: UUID? = null, factoryId: UUID? = null): ItemStack {
        return itemStack.name(name, playerId, shopId, plotId, factoryId).description(description, playerId, shopId, plotId, factoryId).clone()
    }

    suspend fun resolveSuccess(playerId: UUID, success: ServiceResult.Success) {
        success.translatable?.let { sendMiniMessage(playerId, it) }
        success.soundKey?.let { sendSound(playerId, it) }
        success.action?.let { it() }
    }

    suspend fun resolveFailure(playerId: UUID, failure: ServiceResult.Failure) {
        failure.translatable?.let { sendMiniMessage(playerId, it) }
        failure.soundKey?.let { sendSound(playerId, it) }
        failure.action?.let { it() }
    }

    suspend fun sendMiniMessage(playerId: UUID, message: Translatable, shopId: UUID? = null, plotId: UUID? = null) {
        val component = deserialise(message, playerId = playerId, shopId = shopId, plotId = plotId)

        playerService.sendMessage(playerId, component)
    }

    suspend fun sendMiniTitle(playerId: UUID, titleMessage: Translatable, subTitleMessage: Translatable, shopId: UUID? = null, plotId: UUID? = null) {
        val componentOne = deserialise(titleMessage, playerId = playerId, shopId = shopId, plotId = plotId)
        val componentTwo = deserialise(subTitleMessage, playerId = playerId, shopId = shopId, plotId = plotId)

        playerService.showTitle(playerId, Title.title(componentOne, componentTwo))
    }

    suspend fun sendMiniActionBar(playerId: UUID, message: Translatable, shopId: UUID? = null, plotId: UUID? = null) {
        val component = deserialise(message, playerId = playerId, shopId = shopId, plotId = plotId)
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

    private suspend fun deserialise(key: Translatable, playerId: UUID?, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): Component {
        val allTags = TagResolver.resolver(
            getTags(playerId, plotId, shopId, factoryId),
            TagResolver.resolver(key.tags)
        )
        return miniMessage.deserialize(
            plopMessageSource.findMessage(key.key) ?: key.key,
            allTags
        )
    }

    suspend fun deserialiseString(message: String, playerId: UUID, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): Component {
        return miniMessage.deserialize(
            message,
            getTags(playerId, plotId, shopId, factoryId),
        )
    }

    private suspend fun getTags(playerId: UUID?, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): TagResolver {
        return TagResolver.resolver(
            playerTags.all(playerId),
            shopTags.all(playerId, shopId),
            plotTags.all(plotId)
        )
    }

    suspend fun resolveTags(string: String, playerId: UUID?, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): String { // Resolve tags and then un-resolve LOL
        return miniMessage.serialize(miniMessage.deserialize(string, getTags(playerId,plotId,shopId,factoryId)))
    }

    // Item stack
    private suspend fun ItemStack.name(name: Translatable, playerId: UUID?, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(deserialise(name, playerId = playerId, shopId = shopId, plotId = plotId, factoryId = factoryId))
        }
        return this
    }

    private suspend fun ItemStack.description(description: Translatable, playerId: UUID?, plotId: UUID? = null, shopId: UUID? = null, factoryId: UUID? = null): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(deserialise(description, playerId = playerId, shopId = shopId, plotId = plotId, factoryId = factoryId)))
        }
        return this
    }
}