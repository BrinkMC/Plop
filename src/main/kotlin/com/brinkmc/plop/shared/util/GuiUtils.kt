package com.brinkmc.plop.shared.util

import com.brinkmc.plop.Plop
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.reflect.KProperty


/*
Credit to NoxCrew in the interfaces-kotlin library for these handy extensions
 */

object GuiUtils {
    fun ItemStack.name(name: String): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(Component.text(name))
        }
        return this
    }

    fun ItemStack.name(name: Component): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.displayName(name)
        }
        return this
    }

    fun ItemStack.description(description: String): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(Component.text(description)))
        }
        return this
    }

    fun ItemStack.description(description: Component): ItemStack {
        itemMeta = itemMeta.also { meta ->
            meta.lore(listOf(description))
        }
        return this
    }

    /*
    Utility taken from SaveInventory https://github.com/PretzelJohn/SaveInventory/blob/main/src/com/pretzel/dev/saveinventory/lib/Util.java
     */
    fun stacksToBase64(contents: Array<ItemStack?>): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            dataOutput.writeInt(contents.size)
            for (stack in contents) dataOutput.writeObject(stack)
            dataOutput.close()
            return Base64Coder.encodeLines(outputStream.toByteArray()).replace("\n", "").replace("\r", "")
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    /*
    Utility taken from SaveInventory https://github.com/PretzelJohn/SaveInventory/blob/main/src/com/pretzel/dev/saveinventory/lib/Util.java
     */
    fun stacksFromBase64(data: String?): Array<out ItemStack?> {
        if (data == null || Base64Coder.decodeLines(data) == null) return arrayOf()

        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
        var dataInput: BukkitObjectInputStream? = null
        var stacks: Array<ItemStack?>? = null

        try {
            dataInput = BukkitObjectInputStream(inputStream)
            stacks = arrayOfNulls(dataInput.readInt())
        } catch (e: IOException) {
            println(e.message)
        }

        for (i in stacks!!.indices) {
            try {
                stacks[i] = dataInput!!.readObject() as ItemStack
            } catch (e: IOException) {
                try {
                    dataInput!!.close()
                } catch (ignored: IOException) {
                }
                println(e)
            } catch (e: ClassNotFoundException) {
                try {
                    dataInput!!.close()
                } catch (ignored: IOException) {
                }
                println(e)
            }
        }

        try {
            dataInput!!.close()
        } catch (ignored: IOException) {
        }

        return stacks
    }
}