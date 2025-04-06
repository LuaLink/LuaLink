package win.templeos.lualink

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.plugin.java.JavaPlugin
import win.templeos.lualink.lua.LuaManager

class LuaLink : JavaPlugin() {
    private val luaManager = LuaManager(this)

    companion object {
        lateinit var instance: LuaLink
            private set
    }

    override fun onEnable() {
        instance = this
        // Ensure dataFolder+scripts/ exists

        val scriptsDir = dataFolder.resolve("scripts")
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }
        luaManager.loadAllScripts()
        //this.componentLogger.info(MiniMessage.miniMessage().deserialize("<yellow>Attempting to load </yellow><green>${scriptsDir.listFiles()?.size ?: 0}</green><yellow> scripts...</yellow>"))

        // Go through each folder in the scripts directory and try to load main.lua from it
/*        scriptsDir.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                val mainLua = folder.resolve("main.lua")
                if (mainLua.exists()) {
                    val scriptContent = mainLua.readText()
                    luaManager.createScriptFromString(scriptContent, folder.name)
                }
            }
        }*/
    }

    override fun onDisable() {
        luaManager.close()
    }
}