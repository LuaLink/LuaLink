package xyz.galaxyy.lualink.commands

import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import xyz.galaxyy.lualink.api.LuaScript
import xyz.galaxyy.lualink.api.lua.LuaScriptManager
import java.util.*

internal class LoadedScriptParser<C: Any>(private val scriptManager: LuaScriptManager)  : ArgumentParser<C, LuaScript> {
    override fun parse(commandContext: CommandContext<C>, inputQueue: Queue<String>): ArgumentParseResult<LuaScript> {
        val input = inputQueue.peek()
            ?: return ArgumentParseResult.failure(NoInputProvidedException(LoadedScriptParser::class.java, commandContext))
        var script: LuaScript? = null
        this.scriptManager.getLoadedScripts().forEach { loadedScript ->
            if (loadedScript.globals.get("__file_name").tojstring() == input) {
                script = loadedScript
            }
        }
        return if (script != null) {
            inputQueue.remove()

            ArgumentParseResult.success(script!!)
        } else {
            ArgumentParseResult.failure(ScriptParserException(input, commandContext))
        }
    }

    override fun suggestions(commandContext: CommandContext<C>, input: String): MutableList<String> {
        val suggestions = mutableListOf<String>()
        this.scriptManager.getLoadedScripts().forEach { loadedScript ->
            if (loadedScript.globals.get("__file_name").tojstring().startsWith(input)) {
                suggestions.add(loadedScript.globals.get("__file_name").tojstring())
            }
        }
        return suggestions
    }
}