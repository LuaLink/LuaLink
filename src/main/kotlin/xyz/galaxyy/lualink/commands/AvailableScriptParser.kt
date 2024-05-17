package xyz.galaxyy.lualink.commands

import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import xyz.galaxyy.lualink.LuaLink
import xyz.galaxyy.lualink.api.lua.LuaScriptManager
import java.io.File
import java.util.*

class AvailableScriptParser<C: Any>(private val plugin: LuaLink, private val scriptManager: LuaScriptManager)  : ArgumentParser<C, File> {
    override fun parse(commandContext: CommandContext<C>, inputQueue: Queue<String>): ArgumentParseResult<File> {
        val input = inputQueue.peek()
            ?: return ArgumentParseResult.failure(NoInputProvidedException(AvailableScriptParser::class.java, commandContext))
        // If the script is not loaded, and the file exists in dataFolder+/scripts then return the file for loading.
        val file = File(this.plugin.dataFolder, "scripts/$input")
        return if (file.exists()) {
            val loadedScripts = this.scriptManager.getLoadedScripts()
            val isAlreadyLoaded = loadedScripts.any { it.file == file }

            if (!isAlreadyLoaded) {
                // Remove the input from the queue
                inputQueue.remove()

                return ArgumentParseResult.success(file)
            } else {
                ArgumentParseResult.failure(ScriptParserException(input, commandContext))
            }
        } else {
            ArgumentParseResult.failure(ScriptParserException(input, commandContext))
        }
    }

    override fun suggestions(commandContext: CommandContext<C>, input: String): MutableList<String> {
        val suggestions = mutableListOf<String>()
        val scriptsFolder = File(this.plugin.dataFolder, "scripts")
        scriptsFolder.listFiles()?.forEach { file ->
            if (file.name.startsWith(input)) {
                suggestions.add(file.name)
            }
        }
        return suggestions
    }

}