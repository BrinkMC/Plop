package com.brinkmc.plop.shared.service

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.dto.Plot
import com.brinkmc.plop.plot.dto.modifier.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.design.enums.ItemKey
import com.brinkmc.plop.shared.design.enums.MessageKey
import com.brinkmc.plop.shared.design.enums.SoundKey
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

class DesignService(override val plugin: Plop): Addon {

    fun setSkull(itemStack: ItemStack, owner: PlotOwner?): ItemStack {
        val meta = itemStack.itemMeta as SkullMeta
        meta.playerProfile = owner?.getSkull()
        itemStack.itemMeta = meta
        return itemStack
    }

    suspend fun getItem(itemKey: ItemKey, name: MessageKey, description: MessageKey, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        return itemKey.item.name(name, player, shop, plot, *args).description(description, player, shop, plot, *args)
    }

    suspend fun getItem(itemStack: ItemStack, name: MessageKey, description: MessageKey, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        return itemStack.name(name, player, shop, plot, *args).description(description, player, shop, plot, *args)
    }

    suspend fun sendMiniMessage(player: Player, message: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val component = deserialise(message, player = player, shop = shop, plot = plot, args = args)
        player.sendMessage(component)
    }

    suspend fun sendMiniTitle(player: Player, titleMessage: MessageKey, subTitleMessage: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val componentOne = deserialise(titleMessage, player = player, shop = shop, plot = plot, args = args)
        val componentTwo = deserialise(subTitleMessage, player = player, shop = shop, plot = plot, args = args)

        player.showTitle(Title.title(componentOne, componentTwo))
    }

    suspend fun sendMiniActionBar(player: Player, message: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val component = deserialise(message, player = player, shop = shop, plot = plot, args = args)
        player.sendActionBar(component)
    }

    fun sendSound(player: Player, key: SoundKey, volume: Float = 1.0f, pitch: Float = 1.0f) {
        player.playSound(
            Sound.sound(key.key, Sound.Source.MASTER, volume, pitch)
        )
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

    private suspend  fun deserialise(key: MessageKey, player: Player?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(key) ?: key.toString(),
            getTags(player, shop, plot),
            *args
        )
    }

    private suspend fun getTags(player: Player?, shop: Shop? = null, plot: Plot? = null): TagResolver {
        return TagResolver.resolver(
            playerTags.all(player),
            shopTags.all(shop, player),
            plotTags.all(plot)
        )
    }

    private fun resolveTags(string: String, tags: TagResolver): String { // Resolve tags and then un-resolve LOL
        return miniMessage.serialize(miniMessage.deserialize(string, tags))
    }

    // Item stack
    private suspend fun ItemStack.name(name: MessageKey, player: Player?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(deserialise(name, player = player, shop = shop, plot = plot, args = args))
        }
        return this
    }

    private suspend fun ItemStack.description(description: MessageKey, player: Player?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(deserialise(description, player = player, shop = shop, plot = plot, args = args)))
        }
        return this
    }
}