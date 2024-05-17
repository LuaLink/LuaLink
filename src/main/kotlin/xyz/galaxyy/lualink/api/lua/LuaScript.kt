package xyz.galaxyy.lualink.api.lua

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua

import party.iroiro.luajava.Lua.LuaType
import party.iroiro.luajava.value.LuaValue
import xyz.galaxyy.lualink.LuaLink
import xyz.galaxyy.lualink.api.lua.commands.LuaCommandHandler
import java.io.File

// LuaScript contains the Lua script's globals, callbacks, and command and listener handlers and is used to store script state and metadata
class LuaScript(private val plugin: LuaLink, val file: File, val lua: Lua) {
    internal var onLoadCB: LuaValue? = null
        private set

    internal var onUnloadCB: LuaValue? = null
        private set

    internal val commands: MutableList<LuaCommandHandler> = mutableListOf()
    internal val listeners: MutableList<Listener> = mutableListOf()
    // Stores task IDs so they can be cancelled on unload
    internal val tasks: MutableList<Int> = mutableListOf()

    fun onLoad(cb: LuaValue) {
        if (cb.type() == LuaType.FUNCTION) {
            onLoadCB = cb
        } else {
            throw IllegalArgumentException("onLoad callback must be a function")
        }
    }

    fun onUnload(cb: LuaValue) {
        if (cb.type() == LuaType.FUNCTION) {
            onUnloadCB = cb
        } else {
            throw IllegalArgumentException("onUnload callback must be a function")
        }
    }

    fun registerRawCommand(callback: LuaValue, metadata: LuaValue) {
        this.plugin.logger.info("Registering raw command")
        val command = LuaCommandHandler(this.plugin, this, callback, metadata)

        this.commands.add(command)

        this.plugin.server.commandMap.register("lualinkscript", command)
    }

    fun hook(eventName: String, callback: LuaValue) {
        if (eventName.isEmpty() || callback.type() != LuaType.FUNCTION) {
            throw IllegalArgumentException("hook expects 2 arguments: string, function")
        }

        try {
            val eventClass = Class.forName(eventName)
            if (!Event::class.java.isAssignableFrom(eventClass)) {
                throw IllegalArgumentException("Event class must be a subclass of org.bukkit.event.Event")
            }
            val listener = object : Listener {}

            this.plugin.server.pluginManager.registerEvent(eventClass as Class<out org.bukkit.event.Event>, listener, EventPriority.NORMAL, { _, eventObj ->
                if (eventClass.isInstance(eventObj)) {
                    val result = callback.call(eventObj)
                    if (result == null) {
                        this.plugin.logger.severe("Error calling Lua hook for event $eventName: ${this.getLastErrorMessage()}")
                    }
                }
            }, this.plugin)

            this.listeners.add(listener)

        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Event class not found: $eventName")
        }
    }

    fun getServer() = this.plugin.server

    internal fun getLastErrorMessage(): String? {
        var message: String? = null
        if (this.lua.top != 0 && this.lua.isString(-1)) {
            message = this.lua.toString(-1)
        }
        return message
    }

}