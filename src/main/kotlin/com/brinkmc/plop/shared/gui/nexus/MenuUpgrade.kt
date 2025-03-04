package com.brinkmc.plop.shared.gui.nexus

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.noxcrew.interfaces.drawable.Drawable.Companion.drawable
import com.noxcrew.interfaces.element.StaticElement
import com.noxcrew.interfaces.interfaces.buildChestInterface
import com.noxcrew.interfaces.view.InterfaceView
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MenuUpgrade(override val plugin: Plop): Addon {

    val BACK
        get() = ItemStack(Material.REDSTONE)
        .name("menu.back")

    val UPGRADE_TOTEM
        get() = ItemStack(Material.TURTLE_HELMET)
        .name("nexus.upgrade.totem.name")

    val UPGRADE_SHOP
        get() = ItemStack(Material.CHEST)
        .name("nexus.upgrade.shop.name")

    val UPGRADE_VISIT
        get() = ItemStack(Material.OAK_BOAT)
        .name("nexus.upgrade.visit.name")

    val UPGRADE_FACTORY
        get() = ItemStack(Material.FURNACE)
        .name("nexus.upgrade.factory.name")

    val UPGRADE_PLOT
        get() = ItemStack(Material.GRASS_BLOCK)
        .name("nexus.upgrade.size.name")

    private val inventory = buildChestInterface {
        onlyCancelItemInteraction = false
        prioritiseBlockInteractions = false
        rows = 6

        withTransform { pane, view ->
            // Determine plot
            val plot = view.player.getCurrentPlot() ?: return@withTransform

            // Upgrade totem
            pane[3, 2] = StaticElement(drawable(
                UPGRADE_TOTEM.description("nexus.upgrade.totem.description", plot = plot)
            )) { (player) -> plugin.async {
                plots.upgradeHandler.upgradeTotemLevel(plot, player)
            } }
            pane[3, 3] = StaticElement(drawable(
                UPGRADE_SHOP.description("nexus.upgrade.shop.description", plot = plot)
            )) { (player) -> plugin.async {
                plots.upgradeHandler.upgradeShopLevel(plot, player)
            } }
            pane[3, 4] = StaticElement(drawable(
                UPGRADE_VISIT.description("nexus.upgrade.visit.description", plot = plot)
            )) { (player) -> plugin.async {
                plots.upgradeHandler.upgradeVisitorLevel(plot, player)
            } }
            pane[3, 5] = StaticElement(drawable(
                UPGRADE_FACTORY.description("nexus.upgrade.factory.description", plot = plot)
            )) { (player) -> plugin.async {
                plots.upgradeHandler.upgradeFactoryLevel(plot, player)
            } }
            pane[3, 6] = StaticElement(drawable(
                UPGRADE_PLOT.description("nexus.upgrade.size.description", plot = plot)
            )) { (player) -> plugin.async {
                plots.upgradeHandler.upgradeSizeLevel(plot, player)
            } }

            // Back button
            pane[5, 4] = StaticElement(drawable(BACK)) { (player) -> plugin.async {
                view.back()
            } }


        }
    }

    suspend fun open(player: Player, prev: InterfaceView? = null) {
        inventory.open(player, prev)
    }
}