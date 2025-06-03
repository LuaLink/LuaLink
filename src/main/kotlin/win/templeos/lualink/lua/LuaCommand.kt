package win.templeos.lualink.lua

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import party.iroiro.luajava.Lua
import party.iroiro.luajava.value.LuaValue

class LuaCommand(private val command: LuaValue) : Command(command.get("metadata").get("name").toString()) {
    init {
        val metadata = command.get("metadata")
        val description = metadata.get("description")
        val usage = metadata.get("usage")
        val permission = metadata.get("permission")
        val aliases = metadata.get("aliases")
        if (description.type() == Lua.LuaType.STRING) {
            this.description = description.toString()
        }
        if (usage.type() == Lua.LuaType.STRING) {
            this.usage = usage.toString()
        }

        if (permission.type() == Lua.LuaType.STRING) {
            this.permission = permission.toString()
        }

        if (aliases.type() == Lua.LuaType.TABLE) {
            val aliasList = aliases.toJavaObject() as Map<*, *>
            this.aliases = aliasList.values.map { it.toString() }.toMutableList()
        }
    }
    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>?): Boolean {
        command.get("handler").call(sender, args)
        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>?): MutableList<String> {
        val tabCompleteHandler = command.get("metadata").get("tabComplete")
        if (tabCompleteHandler.type() != Lua.LuaType.FUNCTION) {
            // Default behavior: Return online player names
            return Bukkit.getOnlinePlayers().map { player -> player.name }.toMutableList()
        }
        val result = tabCompleteHandler.call(sender, args)[0]
        if (result.type() == Lua.LuaType.TABLE) {
            val list = result.toJavaObject() as HashMap<*, *>
            return (if (args != null) list.values.filter { it.toString().contains(args.last(), true) } else list.values).toMutableList() as MutableList<String>
        }
        // Default behavior: Return online player names
        return null!!
    }
}