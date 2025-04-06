local Material = java.import "org.bukkit.Material"
local ItemStack = java.import "org.bukkit.inventory.ItemStack"

local ITEMS = {
    ItemStack:of(Material.WOODEN_SWORD),
    ItemStack:of(Material.WOODEN_PICKAXE),
    ItemStack:of(Material.WOODEN_AXE),
    ItemStack:of(Material.WOODEN_SHOVEL),
    ItemStack:of(Material.APPLE, 16),
}

-- Listening to PlayerJoinEvent in attempt to give player items when they join for the first time.
script:registerEvent("org.bukkit.event.player.PlayerJoinEvent", function(event)
    -- NOTE: Priting player name for debug purposes, as to make sure that event:getPlayer() works as expected.
    print(event:getPlayer():getName())
    -- NOTE: Condition is currently inverted as I don't feel like deleting player data each time I want to test the code.
    if (event:getPlayer():hasPlayedBefore() == true) then
        -- Getting player's inventory.
        local inventory = event:getPlayer():getInventory()

        -- ItemStack#addItem is a varargs method, so we need to use java.method to call it with an array.
        local addItemMethod = java.method(inventory, "addItem", "org.bukkit.inventory.ItemStack[]")

        -- Create an array of ItemStack objects
        local items = java.array(ItemStack, #ITEMS)
        for i, item in ipairs(ITEMS) do
            items[i] = item -- Copy the item to the array
        end    
        -- Call the addItem method with the array
        addItemMethod(items)
    end
end)