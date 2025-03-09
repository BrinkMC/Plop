package com.brinkmc.plop.shared.util.message

enum class MessageKey(val key: String) {
    NOT_PLOT("plot.no_plot"),
    NOT_OWNER("plot.not-owner"),
    NOT_VISITABLE("plot.not_visitable"),


    TELEPORT_IN_PROGRESS("plot.teleport.progress"),
    TELEPORT_INTERRUPTED("plot.teleport.interrupted"),
    TELEPORT_FAILED("plot.teleport.failed"),
    TELEPORT_COMPLETE("plot.teleport.complete"),
}