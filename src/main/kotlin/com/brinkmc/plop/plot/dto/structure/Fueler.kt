package com.brinkmc.plop.plot.dto.structure

import com.brinkmc.plop.factory.dto.Augment
import com.brinkmc.plop.plot.constant.FuelerType
import org.bukkit.inventory.ItemStack
import java.util.UUID



data class Fueler(
    private val id: UUID,
    private val fuelerType: FuelerType,
    private val location: String,
    private var _item: ItemStack,
    private var _quantity: Int,
    private var _active: Boolean,
    private var _augments: List<Augment>, // two slots?
) {
    fun getId(): UUID {
        return id
    }

    fun getFuelerType(): FuelerType {
        return fuelerType
    }

    fun getLocation(): String {
        return location
    }

    fun getItem(): ItemStack {
        return _item
    }

    fun setItem(item: ItemStack) {
        _item = item
    }

    fun getQuantity(): Int {
        return _quantity
    }

    fun setQuantity(quantity: Int) {
        _quantity = quantity
    }

    fun addQuantity(quantity: Int) {
        _quantity += quantity
        if (_quantity > 0 && !_active) {
            enable()
        }
    }

    fun removeQuantity(quantity: Int) {
        _quantity -= quantity
        if (_quantity < 0) {
            _quantity = 0
            disable()
        }
    }

    private fun disable() {
        _active = false
    }

    private fun enable() {
        _active = true
    }

    fun isActive(): Boolean {
        return _active
    }
}


