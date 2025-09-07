package com.brinkmc.plop.shared.command.utils

import com.brinkmc.plop.plot.constant.PlotType
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.incendo.cloud.suggestion.Suggestion

class PlotTypeParser<CommandSourceStack>: ArgumentParser<CommandSourceStack, PlotType>, BlockingSuggestionProvider<io.papermc.paper.command.brigadier.CommandSourceStack> {

    companion object {
        fun plotTypeParser(): ParserDescriptor<CommandSourceStack, PlotType> {
            return ParserDescriptor.of(PlotTypeParser(), PlotType::class.java)
        }
    }

    override fun parse(
        commandContext: CommandContext<CommandSourceStack & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<PlotType> {

        val input = commandInput.readStringSkipWhitespace()

        return when (input) {
            "personal" -> {
                ArgumentParseResult.success(PlotType.PERSONAL)
            }
            "guild" -> {
                ArgumentParseResult.success(PlotType.GUILD)
            }
            else -> ArgumentParseResult.failure(Exception("Invalid plot type"))
        }
    }

    override fun suggestions(
        context: CommandContext<io.papermc.paper.command.brigadier.CommandSourceStack?>,
        input: CommandInput
    ): Iterable<Suggestion> {
        return listOf("personal", "guild").map { Suggestion.suggestion(it) }
    }
}