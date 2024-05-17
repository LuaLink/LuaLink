package xyz.galaxyy.lualink.api.lua.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua.LuaType
import party.iroiro.luajava.value.LuaValue
import xyz.galaxyy.lualink.LuaLink
import xyz.galaxyy.lualink.api.lua.LuaScript


class LuaCommandHandler(private val plugin: LuaLink, private val script: LuaScript, private val callback: LuaValue, private val metadata: LuaValue) : Command(metadata.get("name").toString()) {
    init {
        if (this.metadata.get("description").type() == LuaType.NIL)
            this.description = this.metadata.get("description").toJavaObject() as String
        if (this.metadata.get("usage").type() != LuaType.NIL)
            this.usage = this.metadata.get("usage").toJavaObject() as String
        if (this.metadata.get("permission").type() != LuaType.NIL)
            this.permission = this.metadata.get("permission").toJavaObject() as String
        if (this.metadata.get("aliases").type() == LuaType.NIL) {
            this.aliases = mutableListOf()
        } else {
            this.aliases = this.metadata.get("aliases").toJavaObject() as MutableList<String>
        }
    }

    private fun call(sender: CommandSender, args: Array<out String>?) {
        val result = this.callback.call(sender, args)
        if (result == null) {
            if (sender.hasPermission("lualink.debug")) {
                sender.sendRichMessage("<red>LuaLink encountered an error while executing the command: ${this.script.getLastErrorMessage()}")
            } else {
                sender.sendRichMessage("<red>An error occurred while executing the command.")
            }
            this.plugin.logger.severe("LuaLink encountered an error while executing a command ${this.name}: ${this.script.getLastErrorMessage()}")
        }
    }
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
        if (this.metadata.get("consoleOnly").toJavaObject() as Boolean && sender !is ConsoleCommandSender) {
            sender.sendRichMessage("<red>This command can only be executed by console.")
            return true
        }
        if (this.metadata.get("playerOnly").toJavaObject() as Boolean && sender !is Player) {
            sender.sendRichMessage("<red>This command can only be executed by a player.")
            return true
        }

        if (this.metadata.get("runAsync").toJavaObject() as Boolean) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, Runnable {
                call(sender, args)
            })
        } else {
            call(sender, args)
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>?): MutableList<String> {
        val tabCompleteFunction = this.metadata.get("tabComplete")

        if (tabCompleteFunction.type() == LuaType.FUNCTION) {

            val luaResult = tabCompleteFunction.call(sender, alias, args)

            if (luaResult[0]?.type() == LuaType.TABLE) {
                return luaResult[0]?.toJavaObject() as MutableList<String>
            }
        }

        // Default behavior: Return online player names
        return Bukkit.getOnlinePlayers().map { player -> player.name }.toMutableList()
    }



}