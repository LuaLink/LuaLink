package win.templeos.lualink.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import win.templeos.lualink.lua.LuaManager

class ServerLoadListener(private val luaManager: LuaManager) : Listener {
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        this.luaManager.loadAllScripts()
    }
}