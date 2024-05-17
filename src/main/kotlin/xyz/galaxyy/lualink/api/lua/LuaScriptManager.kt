package xyz.galaxyy.lualink.api.lua

import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import party.iroiro.luajava.Lua
import party.iroiro.luajava.luajit.LuaJit
import party.iroiro.luajava.value.LuaValue
import xyz.galaxyy.lualink.LuaLink
import java.io.File

class LuaScriptManager(private val plugin: LuaLink) {
    private val loadedScripts: MutableList<LuaScript> = mutableListOf()

    fun getLoadedScripts(): List<LuaScript> {
        return loadedScripts.toList()
    }

    fun loadScript(file: File) {
        val lua: Lua =
            LuaJit() // Perhaps a way to have a script specify which Lua implementation to use? (e.g. LuaJit, Lua 5.4, etc.)
        val script: LuaScript =
            LuaScript(this.plugin, file, lua) // Script contains some state and metadata about the script
        lua.openLibraries()
        lua.push(script, Lua.Conversion.NONE)
        lua.setGlobal("script")
        lua.push(LuaScheduler(this.plugin, script), Lua.Conversion.NONE)
        lua.setGlobal("scheduler")
        this.plugin.logger.info("Loading script ${file.name}")
        val code = file.readText()
        val err = lua.run(code)
        if (err != Lua.LuaError.OK) {
            this.plugin.logger.severe("LuaLink encountered an error while loading ${file.name}: ${script.getLastErrorMessage()}")
            lua.close()
            return
        }
        loadedScripts.add(script)
        if (script.onLoadCB != null) {
            script.onLoadCB?.call()
        }
        Bukkit.getServer().javaClass.getMethod("syncCommands").invoke(Bukkit.getServer())
        this.plugin.logger.info("Loaded script ${file.name}")
    }

    fun unLoadScript(script: LuaScript) {
        script.listeners.forEach { listener ->
            HandlerList.unregisterAll(listener)
        }
        script.commands.forEach { command ->
            command.unregister(this.plugin.server.commandMap)
            this.plugin.server.commandMap.knownCommands.remove(command.name)
            command.aliases.forEach { alias ->
                this.plugin.server.commandMap.knownCommands.remove(alias)
            }
            Bukkit.getServer().javaClass.getMethod("syncCommands").invoke(Bukkit.getServer())
        }
        script.tasks.forEach { taskId ->
            Bukkit.getScheduler().cancelTask(taskId)
        }
        if (script.onUnloadCB != null) {
            script.onUnloadCB?.call()
        }
        script.lua.close()
        this.loadedScripts.remove(script)
    }

    fun disableScript(script: LuaScript) {
        script.file.renameTo(File(script.file.path + ".d"))
        this.unLoadScript(script)
    }

    fun enableScript(script: File) {
        script.renameTo(File(script.path.removeSuffix(".d")))
        this.loadScript(File(script.path.removeSuffix(".d")))
    }

    fun loadScripts() {
        this.plugin.logger.info("Loading scripts...")
        if (!File(this.plugin.dataFolder.path + "/scripts").exists()) {
            File(this.plugin.dataFolder.path + "/scripts").mkdirs()
        }

        File(this.plugin.dataFolder.path + "/scripts").walk().forEach { file ->
            if (file.extension == "lua") {
                if (file.name.startsWith(".")) {
                    return@forEach
                }
                this.loadScript(file)
            } else {
                if (file.name != "scripts") {
                    if (file.name.endsWith(".d"))
                        return@forEach
                    this.plugin.logger.warning("${file.name} is in the scripts folder but is not a lua file!")
                }
            }
        }
    }
}