package xyz.galaxyy.lualink.commands

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.specifier.Greedy
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.galaxyy.lualink.LuaLink
import xyz.galaxyy.lualink.api.lua.LuaScript
import xyz.galaxyy.lualink.api.lua.LuaScriptManager
import java.io.File

@Suppress("unused")
class LuaLinkCommands(private val plugin: LuaLink, private val scriptManager: LuaScriptManager) {
    @CommandDescription("Reload a Lua script")
    @CommandMethod("lualink reload <script>")
    @CommandPermission("lualink.scripts.reload")
    fun reloadScript(sender: CommandSender, @Argument("script") script: LuaScript) {
        val fileName = script.file.name
        this.scriptManager.unLoadScript(script)
        this.scriptManager.loadScript(File(this.plugin.dataFolder, "scripts/$fileName"))
        sender.sendRichMessage("<green>Reloaded script <yellow>$fileName<green>.")
    }

    @CommandDescription("Unload a Lua script")
    @CommandMethod("lualink unload <script>")
    @CommandPermission("lualink.scripts.unload")
    fun unloadScript(sender: CommandSender, @Argument("script") script: LuaScript) {
        this.scriptManager.unLoadScript(script)
        sender.sendRichMessage("<green>Unloaded script <yellow>${script.file.name}<green>.")
    }

    @CommandDescription("Load a Lua script")
    @CommandMethod("lualink load <script>")
    @CommandPermission("lualink.scripts.load")
    fun loadScript(sender: CommandSender, @Argument("script") script: File) {
        this.scriptManager.loadScript(script)
        sender.sendRichMessage("<green>Loaded script <yellow>${script.name}<green>.")
    }

    @CommandDescription("Disable a Lua script")
    @CommandMethod("lualink disable <script>")
    @CommandPermission("lualink.scripts.disable")
    fun disableScript(sender: CommandSender, @Argument("script") script: LuaScript) {
        scriptManager.disableScript(script)
        sender.sendRichMessage("<green>Disabled and unloaded script <yellow>${script.file.name}<green>.")
    }

    @Suggestions("disabledScripts")
    @Suppress("unused")
    fun disabledScriptsSuggestions(sender: CommandContext<Player?>, input: String): List<String> {
        val suggestions = mutableListOf<String>()
        val scriptsFolder = File(this.plugin.dataFolder, "scripts")
        scriptsFolder.listFiles()?.forEach { file ->
            if (file.name.endsWith(".d") && file.name.startsWith(input)) {
                suggestions.add(file.name)
            }
        }
        return suggestions
    }

    @CommandDescription("Enable a Lua script")
    @CommandMethod("lualink enable <script>")
    @CommandPermission("lualink.scripts.enable")
    fun enableScript(sender: CommandSender, @Argument("script", suggestions = "disabledScripts") script: File) {
        scriptManager.enableScript(script)
        sender.sendRichMessage("<green>Enabled and loaded script <yellow>${script.name}<green>.")
    }

    @CommandDescription("Run Lua code")
    @CommandMethod("lualink run <code>")
    @CommandPermission("lualink.scripts.run")
    fun runCode(sender: CommandSender, @Argument("code") @Greedy code: String) {
    }
}