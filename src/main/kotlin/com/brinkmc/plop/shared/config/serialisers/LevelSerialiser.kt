package com.brinkmc.plop.shared.config.serialisers

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

class LevelSerialiser: TypeSerializer<Level> {
    override fun deserialize(type: Type, node: ConfigurationNode): Level? {
        return Level(node.node("Value")?.int, node.node("Price")?.int)
    }

    override fun serialize(type: Type, obj: Level?, node: ConfigurationNode) {
        node.set("Value").set(obj?.value)
        node.set("Price").set(obj?.price)
    }
}