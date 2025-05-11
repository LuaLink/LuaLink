package win.templeos.lualink.config

enum class LuaRuntimes {
    LUAJIT,
    LUA54
}

data class LuaLinkConfig(
    val luaRuntime: LuaRuntimes
)
