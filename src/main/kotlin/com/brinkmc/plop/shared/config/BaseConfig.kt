package com.brinkmc.plop.shared.config

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.spongepowered.configurate.ConfigurationNode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
/*
Arguably the most convoluted configuration class I've ever seen built on top of an even more ridiculous framework
 */
abstract class BaseConfig(override val plugin: Plop): Addon, State {

    protected abstract val config: ConfigurationNode?

    protected val resultCache = hashMapOf<String, Any>()

    override suspend fun load() {} // The config values will load dynamically

    override suspend fun kill() {
        resultCache.clear()
    }

    protected inline fun <reified T> getOrPutAny(property: KProperty<*>, vararg node: Any): T { // Inline to reduce overhead by putting the function where it's called with bytecode
        return resultCache.getOrPut(property.name) { // Cache 1
            config?.node(node)?.get(T::class.java) ?: "" // Allows use of corresponding configuration node to get the correct config value
        } as T
    }

    protected fun <T> setNodeValue(property: KProperty<*>, value: T, vararg node: Any) { // Set the value for the node NOT SAVE
        config?.node(node)?.set(value)
        resultCache[property.name] = value as Any // Update the value in cache as well
    }

    protected inline fun <reified T> delegate(vararg node: Any) = object : ReadWriteProperty<Any?, T> { // Save me even more work for the config
        override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return getOrPutAny(property, node) // Getter for each value
        }

        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            setNodeValue(property, value, node) // Setter for each value
        }
    }
}