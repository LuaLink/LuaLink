package xyz.galaxyy.lualink.api.lua

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import party.iroiro.luajava.Lua
import party.iroiro.luajava.value.LuaValue
import xyz.galaxyy.lualink.LuaLink

class LuaScheduler(private val plugin: LuaLink, private val script: LuaScript) {
    fun run(callback: LuaValue) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("run expects 1 argument: function")
        }

        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTask(plugin)
        script.tasks.add(task.taskId)
    }

    fun runAsync(callback: LuaValue) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("runAsync expects 1 argument: function")
        }

        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTaskAsynchronously(plugin)
        script.tasks.add(task.taskId)
    }

    fun runDelayed(callback: LuaValue, delay: Long) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("runDelayed expects 2 arguments: function, delay")
        }
        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTaskLater(plugin, delay)
        script.tasks.add(task.taskId)
    }

    fun runDelayedAsync(callback: LuaValue, delay: Long) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("runDelayedAsync expects 2 arguments: function, delay")
        }
        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTaskLaterAsynchronously(plugin, delay)
        script.tasks.add(task.taskId)
    }

    fun runRepeating(callback: LuaValue, delay: Long, period: Long) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("runRepeating expects 3 arguments: function, delay, period")
        }
        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTaskTimer(plugin, delay, period)
        script.tasks.add(task.taskId)
    }

    fun runRepeatingAsync(callback: LuaValue, delay: Long, period: Long) {
        if (callback.type() != Lua.LuaType.FUNCTION) {
            throw IllegalArgumentException("runRepeatingAsync expects 3 arguments: function, delay, period")
        }
        val task = object : BukkitRunnable() {
            override fun run() {
                callback.call(this)
            }
        }.runTaskTimerAsynchronously(plugin, delay, period)
        script.tasks.add(task.taskId)
    }

    fun cancelTask(taskId: Int) {
        Bukkit.getScheduler().cancelTask(taskId)
        if (script.tasks.contains(taskId))
            script.tasks.remove(taskId)
    }
}