local GameMode = java.import "org.bukkit.GameMode"

local function setGameMode(player, mode)
    
    if player == "@a" then
        for _, onlinePlayer in ipairs(totable(server:getOnlinePlayers())) do
            onlinePlayer:setGameMode(mode)
        end
    else
        if type(player) == "string" then
            player = server:getPlayer(player)
        end
        if player == nil then
            return "<red>Player not found"
        end
        player:setGameMode(mode)
    end

    return nil  -- Success, no error message
end

local function sendMessage(sender, target, mode)
    if target == "@a" then
        sender:sendRichMessage("<green>Game mode set to <yellow>" .. mode .. " <green>for all players")
        return
    end

    if sender:getName() == target then
        sender:sendRichMessage("<green>Game mode set to <yellow>" .. mode)
    else
        sender:sendRichMessage("<green>Game mode set to <yellow>" .. mode .. " <green>for <yellow>" .. target)
    end
end


-- GMC
script:registerCommand(function(sender, args)
    local playerName;
    if #args > 0 then
        playerName = args[1]
    end
    if playerName ~= nil and playerName ~= sender:getName() and not sender:hasPermission("minecraft.command.gamemode.creative.other") then
        sender:sendRichMessage("<red>You do not have permission to change the gamemode of other players")
        return
    end
    local result = setGameMode(playerName or sender, GameMode.CREATIVE)
    if result then
        sender:sendRichMessage(result)
    else
        sendMessage(sender, playerName or sender:getName(), "Creative")
    end
end, {
    name = "gmc",
    description = "Set game mode to Creative",
    usage = "/gmc [player]",
    permission = "minecraft.command.gamemode.creative"
})

-- GMS
script:registerCommand(function(sender, args)
    local playerName;
    if #args > 0 then
        playerName = args[1]
    end
    if playerName ~= nil and playerName ~= sender:getName() and not sender:hasPermission("minecraft.command.gamemode.survival.other") then
        sender:sendRichMessage("<red>You do not have permission to change the gamemode of other players")
        return
    end
    local result = setGameMode(playerName or sender, GameMode.SURVIVAL)
    if result then
        sender:sendRichMessage(result)
    else
        sendMessage(sender, playerName or sender:getName(), "Survival")
    end
end, {
    name = "gms",
    description = "Set game mode to Survival",
    usage = "/gms [player]",
    permission = "minecraft.command.gamemode.survival"
})

-- GMSP

script:registerCommand(function(sender, args)
    local playerName;
    if #args > 0 then
        playerName = args[1]
    end

    if playerName ~= nil and playerName ~= sender:getName() and not sender:hasPermission("minecraft.command.gamemode.spectator.other") then
        sender:sendRichMessage("<red>You do not have permission to change the gamemode of other players")
        return
    end
    local result = setGameMode(playerName or sender, GameMode.SPECTATOR)
    if result then
        sender:sendRichMessage(result)
    else
        sendMessage(sender, playerName or sender:getName(), "Spectator")
    end
end, {
    name = "gmsp",
    description = "Set game mode to Spectator",
    usage = "/gmsp [player]",
    permission = "minecraft.command.gamemode.spectator"
})

-- GMA

script:registerCommand(function(sender, args)
    local playerName;
    if #args > 0 then
        playerName = args[1]
    end

    if playerName ~= nil and playerName ~= sender:getName() and not sender:hasPermission("minecraft.command.gamemode.adventure.other") then
        sender:sendRichMessage("<red>You do not have permission to change the gamemode of other players")
        return
    end
    local result = setGameMode(playerName or sender, GameMode.ADVENTURE)
    if result then
        sender:sendRichMessage(result)
    else
        sendMessage(sender, playerName or sender:getName(), "Adventure")
    end
end, {
    name = "gma",
    description = "Set game mode to Adventure",
    usage = "/gma [player]",
    permission = "minecraft.command.gamemode.adventure"
})