package com.brinkmc.plop.plot.constant

import com.brinkmc.plop.shared.constant.ItemKey
import com.brinkmc.plop.shared.constant.MessageInstance
import com.brinkmc.plop.shared.constant.MessageKey
import com.brinkmc.plop.shared.constant.TagKey
import com.brinkmc.plop.shared.constant.Translatable
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

interface Actionable {
    val item: ItemStack
    val logName: Translatable
    val logDesc: Translatable
}

enum class PlotAction(override val item: ItemStack, override val logName: MessageKey, override val logDesc: MessageKey): Actionable {
    CREATE_PLOT(
        ItemStack(Material.SLIME_SPAWN_EGG),
        MessageKey.LOG_CREATE_PLOT_NAME,
        MessageKey.LOG_CREATE_PLOT_DESC,
    ),
    DELETE_PLOT(
        ItemStack(Material.MAGMA_CUBE_SPAWN_EGG),
        MessageKey.LOG_DELETE_PLOT_NAME,
        MessageKey.LOG_DELETE_PLOT_DESC,
    ),
    PLACE_SHOP(
        ItemStack(Material.CHEST),
        MessageKey.LOG_PLACE_SHOP_NAME,
        MessageKey.LOG_PLACE_SHOP_DESC,
    ),
    REMOVE_SHOP(
        ItemStack(Material.HOPPER),
        MessageKey.LOG_REMOVE_SHOP_NAME,
        MessageKey.LOG_REMOVE_SHOP_DESC,
    );

    operator fun invoke(values: Map<TagKey, Any>): Actionable {
        return PlotActionInstance(
            item = this.item,
            logName = logName(values), // Returns a MessageInstance (Translatable)
            logDesc = logDesc(values)   // Returns a MessageInstance (Translatable)
        )
    }

    operator fun invoke(vararg pairs: Pair<TagKey, Any>) = invoke(pairs.toMap())
}

data class PlotActionInstance(
    override val item: ItemStack,
    override val logName: Translatable,
    override val logDesc: Translatable
) : Actionable