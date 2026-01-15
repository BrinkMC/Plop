package com.brinkmc.plop.shared.constant

enum class PermissionKey(val permission: String) {

    // Plot permissions

    USE_NEXUS("plot.use.nexus"),
    PLOT_SET_HOME("plot.set.home"),
    PLOT_TELEPORT_HOME("plot.teleport.home"),
    PLOT_SET_VISIT("plot.set.visit"),
    PLOT_TELEPORT_VISIT("plot.teleport.visit"),
    PLOT_UPGRADE("plot.upgrade"),

    GET_NEXUS_BOOK("plot.nexus.book"),


    // Claim permissions
    PLOT_CLAIM("plot.claim"),
}