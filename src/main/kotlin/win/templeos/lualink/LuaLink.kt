package win.templeos.lualink

import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin
import win.templeos.lualink.lua.LuaManager

class LuaLink : JavaPlugin() {
    companion object {
        private const val PLUGIN_ID = 25540
    }
    private val luaManager = LuaManager(this)
    private lateinit var metrics: Metrics

    override fun onEnable() {
        luaManager.loadAllScripts()
        this.enableBStats()
    }

    override fun onDisable() {
        luaManager.close()
    }

    private fun enableBStats() {
        val metrics: Metrics = Metrics(this, PLUGIN_ID)
    }
}