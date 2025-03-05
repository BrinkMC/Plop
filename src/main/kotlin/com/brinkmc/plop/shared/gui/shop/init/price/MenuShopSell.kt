package com.brinkmc.plop.shared.gui.shop.init.price

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.isDrop
import com.brinkmc.plop.shop.shop.Shop
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildCombinedInterface
import com.noxcrew.interfaces.properties.interfaceProperty
import com.noxcrew.interfaces.view.InterfaceView
import kotlinx.coroutines.CompletableDeferred
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuShopSell(override val plugin: Plop): Addon {

    private val completion = mutableMapOf<Player, CompletableDeferred<Shop?>>()

    val BACK: ItemStack
        get() = ItemStack(Material.REDSTONE)
            .name("shop.back-stock.name")
            .description("shop.back-stock.desc")

    val CONFIRM: ItemStack
        get() = ItemStack(Material.EMERALD)
            .name("shop.confirm-stock.name")
            .description("shop.confirm-stock.desc")

    val MORE: ItemStack
        get() = ItemStack(Material.GOLD_INGOT)
            .name("shop.more-amount.name")
            .description("shop.more-amount.desc")

    val LESS: ItemStack
        get() = ItemStack(Material.GOLD_NUGGET)
            .name("shop.less-amount.name")
            .description("shop.less-amount.desc")

    val BAD: ItemStack
        get() = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            .name("shop.bad-amount.name")
            .description("shop.bad-amount.desc")

    private fun inventory(player: Player, inputShop: Shop) = buildCombinedInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false

        rows = 5

        val shopProperty = interfaceProperty(inputShop)
        var shop by shopProperty

        var unitMultiplierProperty = interfaceProperty(1)
        var unitMultiplier by unitMultiplierProperty

        // Confirm
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->
            pane[1, 4] = if (shop.sellPrice <= 0.0f) {
                StaticElement(drawable(
                    BAD
                ))
            } else {
                StaticElement(drawable(
                    CONFIRM
                )) { (player) ->
                    plugin.async {
                        completion[player]?.complete(shop)
                        view.close()
                    }
                }
            }
        }

        // Increase
        withTransform(shopProperty, unitMultiplierProperty) { pane, view ->

            val multiplierPlaceholder = Placeholder.component("multiplier", Component.text(unitMultiplier))

            pane[1, 6] = StaticElement(drawable(
                MORE.name("shop.more-amount.name", args = arrayOf(multiplierPlaceholder)).description("shop.more-amount.desc", args = arrayOf(multiplierPlaceholder))
            )) { (player, view, click) ->
                plugin.async {
                    if (click.isDrop()) {
                        unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                        return@async
                    }
                    shop.setSellPrice(shop.sellPrice + unitMultiplier)
                    view.redrawComplete()
                }
            }
        }

        // Less button
        withTransform(shopProperty) { pane, view ->
            pane[1, 2] = if (0 < shop.sellPrice) {
                StaticElement(drawable(
                    LESS
                )) { (player, view, click) ->
                    plugin.async {
                        if (click.isDrop()) {
                            unitMultiplier = if (unitMultiplier != 10000) unitMultiplier * 10 else 1
                            return@async
                        }
                        val finalPrice = if (shop.sellPrice - unitMultiplier < 0) 0.0f else shop.sellPrice - unitMultiplier
                        shop.setSellPrice(finalPrice)
                        view.redrawComplete()
                    }
                }
            } else {
                StaticElement(drawable(
                    BAD.name("shop.bad-amount.toolittle.name").description("shop.bad-amount.toolittle.desc")
                )) // Do nothing
            }
        }

        // Back button
        withTransform(shopProperty) { pane, view ->
            pane[4, 4] = StaticElement(drawable(
                BACK
            )) { (player) ->
                plugin.async {
                    view.close()
                }
            }
        }

        addCloseHandler { reasons, handler ->
            if (completion[handler.player]?.isCompleted == false) {
                completion[handler.player]?.complete(null) // Finalise with a null if not completed
            }

            if (handler.parent() != null) {
                handler.parent()?.open()
            }

            completion.remove(handler.player)
        }
    }

    suspend fun open(player: Player, shop: Shop, parentView: InterfaceView? = null): Shop? {
        val request = CompletableDeferred<Shop?>()
        completion[player] = request

        inventory(player, shop.getSnapshot()).open(player, parentView)
        return request.await()
    }
}