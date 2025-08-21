package com.brinkmc.plop.shared.util.design

import com.brinkmc.plop.Plop
import com.brinkmc.plop.plot.plot.base.Plot
import com.brinkmc.plop.plot.plot.modifier.PlotOwner
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.util.design.enums.ItemKey
import com.brinkmc.plop.shared.util.design.enums.MessageKey
import com.brinkmc.plop.shared.util.design.enums.SoundKey
import com.brinkmc.plop.shared.util.design.tags.PlayerTags
import com.brinkmc.plop.shared.util.design.tags.PlotTags
import com.brinkmc.plop.shared.util.design.tags.ShopTags
import com.brinkmc.plop.shop.shop.Shop
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import net.kyori.adventure.title.Title
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class DesignHandler(override val plugin: Plop): Addon {

    fun setSkull(itemStack: ItemStack, owner: PlotOwner?): ItemStack {
        val meta = itemStack.itemMeta as SkullMeta
        meta.playerProfile = owner?.getSkull()
        itemStack.itemMeta = meta
        return itemStack
    }

    fun getItem(itemKey: ItemKey, name: MessageKey, description: MessageKey, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        return itemKey.item.name(name, player, shop, plot, *args).description(description, player, shop, plot, *args)
    }

    fun getItem(itemStack: ItemStack, name: MessageKey, description: MessageKey, player: Player? = null, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        return itemStack.name(name, player, shop, plot, *args).description(description, player, shop, plot, *args)
    }

    fun sendMiniMessage(audience: Audience, message: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val component = deserialise(message, audience = audience, shop = shop, plot = plot, args = args)
        audience.sendMessage(component)
    }

    fun sendMiniTitle(audience: Audience, titleMessage: MessageKey, subTitleMessage: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val componentOne = deserialise(titleMessage, audience = audience, shop = shop, plot = plot, args = args)
        val componentTwo = deserialise(subTitleMessage, audience = audience, shop = shop, plot = plot, args = args)

        audience.showTitle(Title.title(componentOne, componentTwo))
    }

    fun sendMiniActionBar(audience: Audience, message: MessageKey, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver) {
        val component = deserialise(message, audience = audience, shop = shop, plot = plot, args = args)
        audience.sendActionBar(component)
    }

    fun sendSound(audience: Audience, key: SoundKey, volume: Float = 1.0f, pitch: Float = 1.0f) {
        audience.playSound(
            Sound.sound(key.key, Source.MASTER, volume, pitch)
        )
    }

    // Private

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

    private fun deserialise(key: MessageKey, audience: Audience?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): Component {
        return miniMessage.deserialize(
            plopMessageSource.findMessage(key) ?: key.toString(),
            getTags(audience, shop, plot),
            *args
        )
    }

    private fun getTags(audience: Audience?, shop: Shop? = null, plot: Plot? = null): TagResolver {
        return TagResolver.resolver(
            playerTags.all(audience),
            shopTags.all(shop),
            plotTags.all(plot)
        )
    }

    private fun resolveTags(string: String, tags: TagResolver): String { // Resolve tags and then un-resolve LOL
        return miniMessage.serialize(miniMessage.deserialize(string, tags))
    }




    // Item stack
    private fun ItemStack.name(name: MessageKey, audience: Audience?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(deserialise(name, audience = audience, shop = shop, plot = plot, args = args))
        }
        return this
    }

    private fun ItemStack.description(description: MessageKey, audience: Audience?, shop: Shop? = null, plot: Plot? = null, vararg args: TagResolver): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(deserialise(description, audience = audience, shop = shop, plot = plot, args = args)))
        }
        return this
    }
}





