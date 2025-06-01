---@class Scheduler
---@field plugin string The plugin instance
---@field script Script The script instance
---@field tasks table The list of scheduled tasks
Scheduler = {}

--- Create a new scheduler instance
---@param plugin string The plugin instance
---@return Scheduler
function Scheduler.new(plugin, script)
    local self = setmetatable({}, {__index = Scheduler})
    self.plugin = plugin
    self.script = script
    self.tasks = {}

    self.script:onUnload(function()
        -- Unregister all tasks and unref when the script is unloaded
        for _, task in ipairs(self.tasks) do
            local taskId = task.taskId
            local luaRef = task.luaRef
            if taskId then
                server:getScheduler():cancelTask(taskId)
            end
            if luaRef then
                __unref(luaRef)
            end
        end
        self.tasks = {}
        self.plugin = nil
        self.script = nil 
    end)
    return self
end

--- Schedule a task to run on the next tick
---@param task function The task to run
---@return Task The scheduled task
function Scheduler:run(handler)
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler, true) -- Unref automatically after exec
        return runnable:runTask(self.plugin)
    end)
    if not success then
        self.script.logger:warning("Error scheduling task: " .. err)
        return nil
    end

    return task
end

--- Schedule an asynchronous task to run on the next tick
---@param task function The task to run
---@return Task The scheduled task
function Scheduler:runAsync(handler)
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler, true) -- Unref automatically after exec
        return runnable:runTaskAsynchronously(self.plugin)
    end)
    if not success then
        self.script.logger:warning("Error scheduling async task: " .. err)
        return nil
    end

    return task
end

--- Schedule a task to run after N ticks
---@param task function The task to run
---@param delay number The delay in ticks
---@return Task The scheduled task
function Scheduler:runDelayed(handler, delay)
    local luaRef = nil
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler)
        luaRef = ref
        return runnable:runTaskLater(self.plugin, delay)
    end)
    if not success then
        self.script.logger:warning("Error scheduling delayed task: " .. err)
        return nil
    end

    table.insert(self.tasks, { taskId = task:getTaskId(), luaRef = luaRef })
    return task
end

--- Schedule a task to run after N ticks asynchronously
---@param task function The task to run
---@param delay number The delay in ticks
---@return Task The scheduled task
function Scheduler:runDelayedAsync(handler, delay)
    local luaRef = nil
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler)
        luaRef = ref
        return runnable:runTaskLaterAsynchronously(self.plugin, delay)
    end)
    if not success then
        self.script.logger:warning("Error scheduling delayed async task: " .. err)
        return nil
    end

    table.insert(self.tasks, { taskId = task:getTaskId(), luaRef = luaRef })
    return task
end

--- Schedule a repeating task
---@param task function The task to run
---@param delay number The delay in ticks before the first execution
---@param period number The period in ticks between subsequent executions
---@return Task The scheduled task
function Scheduler:runRepeating(handler, delay, period)
    local luaRef = nil
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler)
        luaRef = ref
        return runnable:runTaskTimer(self.plugin, delay, period)
    end)
    if not success then
        self.script.logger:warning("Error scheduling repeating task: " .. err)
        return nil
    end

    table.insert(self.tasks, { taskId = task:getTaskId(), luaRef = luaRef })
    return task
end

--- Schedule a repeating task asynchronously
---@param task function The task to run
---@param delay number The delay in ticks before the first execution
---@param period number The period in ticks between subsequent executions
---@return Task The scheduled task
function Scheduler:runRepeatingAsync(handler, delay, period)
    local luaRef = nil
    local success, task = pcall(function()
        local runnable, ref = __createRunnable(handler)
        return runnable:runTaskTimerAsynchronously(self.plugin, delay, period)
    end)
    if not success then
        self.script.logger:warning("Error scheduling repeating async task: " .. err)
        return nil
    end

    table.insert(self.tasks, { taskId = task:getTaskId(), luaRef = luaRef })
    return task
end

--- Cancel a scheduled task
---@param task Task|number The task to cancel
function Scheduler:cancel(task)
    if task and type(task) == "number" then
        server:getScheduler():cancelTask(task)
    else 
        task:cancel()
    end
end