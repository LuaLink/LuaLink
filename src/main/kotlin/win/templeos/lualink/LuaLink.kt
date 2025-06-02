package win.templeos.lualink

import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.plugin.java.JavaPlugin
import win.templeos.lualink.config.LuaLinkConfigManager
import win.templeos.lualink.listeners.ServerLoadListener
import win.templeos.lualink.lua.LuaManager

class LuaLink : JavaPlugin() {
    companion object {
        private const val PLUGIN_ID = 25540
    }

    private lateinit var metrics: Metrics
    private val configManager = LuaLinkConfigManager(this.dataFolder.resolve("config.json"))
    private val luaManager = LuaManager(this, configManager.config.luaRuntime)

    override fun onEnable() {
        this.server.pluginManager.registerEvents(ServerLoadListener(luaManager), this)
        this.enableBStats()
    }

    override fun onDisable() {
        luaManager.close()
    }

    private fun enableBStats() {
        val metrics: Metrics = Metrics(this, PLUGIN_ID)
        metrics.addCustomChart(SimplePie("lua_runtime") {
            luaManager.getLuaRuntime()
        })
    }
}