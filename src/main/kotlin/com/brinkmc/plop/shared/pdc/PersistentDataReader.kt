package com.brinkmc.plop.shared.pdc

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import com.brinkmc.plop.shared.pdc.PersistentDataReader.Companion.namespacedKey
import com.google.gson.Gson
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import kotlin.text.get

class PersistentDataReader(override val plugin: Plop): Addon {

    companion object {
        lateinit var namespacedKey: NamespacedKey
    }

    init {
        namespacedKey = NamespacedKey(plugin, "plugin-data")
    }
}

// Companion functions to the general premise
fun <T> ItemStack.retrieveData(dataClass: Class<T>): T? { // Get the data from the item as a desired class
    val rawData = itemMeta?.persistentDataContainer?.get(namespacedKey, PersistentDataType.STRING) // Get the json
    return rawData?.let { Gson().fromJson(it, dataClass) } // Get the data stored in the json
}

fun <T> ItemStack.storeData(data: T): ItemStack {
    val jsonData = Gson().toJson(data) // Jsonify the data
    itemMeta = itemMeta.also { meta ->
        meta.persistentDataContainer.set(namespacedKey, PersistentDataType.STRING, jsonData) // Apply to the item
    }
    return this // Return the item
}