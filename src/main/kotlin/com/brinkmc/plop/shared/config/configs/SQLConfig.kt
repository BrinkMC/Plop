package com.brinkmc.plop.shared.config.configs

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import com.brinkmc.plop.shared.base.State
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import kotlin.reflect.KProperty

@ConfigSerializable
data class SQLConfig(
    @Setting("User")
    val user: String?,
    @Setting("Password")
    val password: String?,
    @Setting("Database")
    val database: String?,
    @Setting("Host")
    val host: String?
)