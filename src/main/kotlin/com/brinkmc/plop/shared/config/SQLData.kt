package com.brinkmc.plop.shared.config

internal data class SQLData(
    var user: String = "username",
    var password: String = "password",
    var database: String = "database",
    var host: String = "host"
)