package win.templeos.lualink

import org.bukkit.plugin.java.JavaPlugin
import win.templeos.lualink.lua.LuaManager

class LuaLink : JavaPlugin() {
    private val luaManager = LuaManager(this)

    override fun onEnable() {
        luaManager.loadAllScripts()
    }

    override fun onDisable() {
        luaManager.close()
    }
}