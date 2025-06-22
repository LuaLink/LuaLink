-- Basic subcommand system for script management
---@param sender Player The player who executed the command
---@param args string[] The command arguments
local subcommands = {
    ["list"] = {
        description = "List all loaded scripts",
        permission = "lualink.command.list",
        usage = "/lualink list",
        handler = function(sender, args)
            local loadedScripts = ScriptManager.getLoadedScripts()
            if #loadedScripts == 0 then
                sender:sendRichMessage("<red>No scripts loaded")
            else
                sender:sendRichMessage("<green>Loaded scripts:")
                for _, scriptName in ipairs(loadedScripts) do
                    if scriptName == "LuaLink" then
                        goto continue
                    end
                    sender:sendRichMessage(" - <yellow>" .. scriptName)

                    ::continue::
                    -- Skips the LuaLink script from being listed
                end
            end
        end
    },
    ["unload"] = {
        description = "Unload a script",
        permission = "lualink.command.unload",
        usage = "/lualink unload <script>",
        handler = function(sender, args)
            if #args < 2 then
                sender:sendRichMessage("<red>Usage: /lualink unload <script>")
                return
            end
            local scriptName = args[2]
            if scriptName == "LuaLink" then
                sender:sendRichMessage("<red>Cannot unload the internal LuaLink script")
                return
            end
            if not ScriptManager.isScriptLoaded(scriptName) then
                sender:sendRichMessage("<red>Script is not loaded: " .. scriptName)
                return
            end
            local success, err = ScriptManager.unloadScript(scriptName)
            if success then
                sender:sendRichMessage("<green>Script unloaded: " .. scriptName)
            else
                sender:sendRichMessage("<red>Error unloading script: " .. (err or "Unknown error"))
            end
        end,
        tabComplete = function(sender, args)
            local completions = {}
            if #args == 2 then
                local loadedScripts = ScriptManager.getLoadedScripts()
                for _, scriptName in ipairs(loadedScripts) do
                    if scriptName ~= "LuaLink" and scriptName:lower():find(args[2]:lower(), 1, true) then
                        table.insert(completions, scriptName)
                    end
                end
            end
            return completions
        end,
    },
    ["load"] = {
        description = "Load a script",
        permission = "lualink.command.load",
        usage = "/lualink load <script>",
        handler = function(sender, args)
            if #args < 2 then
                sender:sendRichMessage("<red>Usage: /lualink load <script>")
                return
            end
            local scriptName = args[2]
            if scriptName == "LuaLink" then
                sender:sendRichMessage("<red>Cannot load the internal LuaLink script")
                return
            end
            -- Check if the script is already loaded
            local loadedScripts = ScriptManager.getLoadedScripts()
            for _, loadedScript in ipairs(loadedScripts) do
                if loadedScript == scriptName then
                    sender:sendRichMessage("<red>Script already loaded: " .. scriptName)
                    return
                end
            end
            local success, err = ScriptManager.loadScript(scriptName)
            if success then
                sender:sendRichMessage("<green>Script loaded: " .. scriptName)
            else
                sender:sendRichMessage("<red>Error loading script: " .. err)
            end
        end,
        tabComplete = function(sender, args)
            local completions = {}
            -- Show all available scripts in the scripts directory but hide the ones that are already loaded
            if #args == 2 then
                local loadedScripts = ScriptManager.getLoadedScripts()
                local allScripts = __getAvailableScripts()
                for _, scriptName in ipairs(allScripts) do
                    local alreadyLoaded = false
                    for _, loadedScript in ipairs(loadedScripts) do
                        if loadedScript == scriptName then
                            alreadyLoaded = true
                            break
                        end
                    end
                    if scriptName ~= "LuaLink" and not alreadyLoaded and scriptName:lower():find(args[2]:lower(), 1, true) then
                        table.insert(completions, scriptName)
                    end
                end
            end
            return completions
        end,
    },
    ["reload"] = {
        description = "Reload a script",
        permission = "lualink.command.reload",
        usage = "/lualink reload <script>",
        handler = function(sender, args)
            if #args < 2 then
                sender:sendRichMessage("<red>Usage: /lualink reload <script>")
                return
            end
            local scriptName = args[2]
            if scriptName == "LuaLink" then
                sender:sendRichMessage("<red>Cannot reload the internal LuaLink script")
                return
            end
            if not ScriptManager.isScriptLoaded(scriptName) then
                sender:sendRichMessage("<red>Script is not loaded: " .. scriptName)
                return
            end
            local success, err = ScriptManager.unloadScript(scriptName)
            if not success then
                sender:sendRichMessage("<red>Error unloading script: " .. (err or "Unknown error"))
                return
            end
            success, err = ScriptManager.loadScript(scriptName)
            if success then
                sender:sendRichMessage("<green>Script reloaded: " .. scriptName)
            else
                sender:sendRichMessage("<red>Error reloading script: " .. (err or "Unknown error"))
            end
        end,
        tabComplete = function(sender, args)
            local completions = {}
            if #args == 2 then
                local loadedScripts = ScriptManager.getLoadedScripts()
                for _, scriptName in ipairs(loadedScripts) do
                    if scriptName ~= "LuaLink" and scriptName:lower():find(args[2]:lower(), 1, true) then
                        table.insert(completions, scriptName)
                    end
                end
            end
            return completions
        end,
    },
}
-- Script management commands
script:registerCommand(function(sender, args)
    -- If no args send a help command with available subcommands with usage and description
    if #args == 0 then
        sender:sendRichMessage("<green>Available commands:")
        for name, command in pairs(subcommands) do
            sender:sendRichMessage(" - <yellow>" .. command.usage .. " <gray>(" .. command.description .. ")")
        end
        return
    end
    -- Check if the first argument is a valid subcommand
    local subcommand = nil
    for name, command in pairs(subcommands) do
        if name == args[1] then
            subcommand = command
            break
        end
    end
    if not subcommand then
        sender:sendRichMessage("<red>Unknown command: " .. args[1])
        return
    end

    -- Check if the player has permission to use the command
    if not sender:hasPermission(subcommand.permission) then
        sender:sendRichMessage("<red>You do not have permission to use this command")
        return
    end

    -- Check if the command has a handler and call it
    if subcommand.handler then
        local success, err = pcall(subcommand.handler, sender, args)
        if not success then
            sender:sendRichMessage("<red>Error executing command: " .. tostring(err))
        end
    else
        sender:sendRichMessage("<red>No handler for command: " .. args[1])
    end
end, {
    name = "lualink",
    description = "LuaLink script management",
    usage = "/lualink [command] <args>",
    permission = "lualink.command",
    tabComplete = function(sender, args)
        local completions = {}
        if #args == 1 then
            for name, command in pairs(subcommands) do
                if name:lower():find(args[1]:lower(), 1, true) then
                    table.insert(completions, name)
                end
            end
        elseif #args > 1 then
            local subcommand = subcommands[args[1]]
            if subcommand and subcommand.tabComplete then
                completions = subcommand.tabComplete(sender, args)
            end
        end
        return completions
    end
})
