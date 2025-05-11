package win.templeos.lualink.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files

class LuaLinkConfigManager(private val configFile: File) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    var config: LuaLinkConfig = LuaLinkConfig(LuaRuntimes.LUAJIT)

    init {
        loadConfig()
    }

    private fun loadConfig() {
        if (configFile.exists()) {
            try {
                config = gson.fromJson(configFile.readText(), LuaLinkConfig::class.java)
            } catch (e: Exception) {
                println("Failed to load config.json: ${e.message}")
                saveConfig() // Save default config if loading fails
            }
        } else {
            saveConfig() // Save default config if file doesn't exist
        }
    }

    fun saveConfig() {
        try {
            Files.createDirectories(configFile.parentFile.toPath())
            configFile.writeText(gson.toJson(config))
        } catch (e: Exception) {
            println("Failed to save config.json: ${e.message}")
        }
    }
}