package xyz.galaxyy.lualink.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import xyz.galaxyy.lualink.lua.LuaScriptManager

internal class ServerLoadListener(private val scriptManager: LuaScriptManager) : Listener {
    @EventHandler
    fun onServerLoad(event: ServerLoadEvent) {
        this.scriptManager.loadScripts()
    }
}