package com.brinkmc.plop.shared.command.processors

import com.brinkmc.plop.Plop
import com.brinkmc.plop.shared.base.Addon
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput

class GeneralSuggestionProcessor(override val plugin: Plop): Addon {

    @Suggestions("plotType")
    fun suggestions(context: CommandContext<*>, input: CommandInput): List<String> {
        return listOf("personal", "guild")
    }
}