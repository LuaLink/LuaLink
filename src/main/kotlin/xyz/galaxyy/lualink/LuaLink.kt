package xyz.galaxyy.lualink

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.arguments.parser.ParserParameters
import cloud.commandframework.arguments.parser.StandardParameters
import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor
import cloud.commandframework.meta.CommandMeta
import cloud.commandframework.paper.PaperCommandManager
import io.leangen.geantyref.TypeToken
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import xyz.galaxyy.lualink.commands.AvailableScriptParser
import xyz.galaxyy.lualink.commands.LoadedScriptParser
import xyz.galaxyy.lualink.commands.LuaLinkCommands
import xyz.galaxyy.lualink.listeners.ServerLoadListener
import xyz.galaxyy.lualink.api.lua.LuaScript
import xyz.galaxyy.lualink.api.lua.LuaScriptManager
import java.io.File
import java.util.function.Function

class LuaLink : JavaPlugin() {
    private lateinit var manager: PaperCommandManager<CommandSender>
    private lateinit var annotationParser: AnnotationParser<CommandSender>
    private val scriptManager: LuaScriptManager = LuaScriptManager(this)
    override fun onEnable() {
        this.server.pluginManager.registerEvents(ServerLoadListener(this.scriptManager), this)
        this.setupCloud()
        this.registerCommands()
    }

    override fun onDisable() {
        val scripts = this.scriptManager.getLoadedScripts()
        scripts.forEach(this.scriptManager::unLoadScript)
    }
    private fun registerCommands() {
        this.annotationParser.parse(LuaLinkCommands(this, this.scriptManager))
    }
    private fun setupCloud() {

            val executionCoordinatorFunction = CommandExecutionCoordinator.simpleCoordinator<CommandSender>()

            val mapperFunction: Function<CommandSender, CommandSender> = Function.identity()
            try {
                this.manager = PaperCommandManager( /* Owning plugin */
                    this,  /* Coordinator function */
                    executionCoordinatorFunction,  /* Command Sender -> C */
                    mapperFunction,  /* C -> Command Sender */
                    mapperFunction
                )
            } catch (e: Exception) {
                logger.severe("Failed to initialize the command this.manager")
                /* Disable the plugin */server.pluginManager.disablePlugin(this)
                return
            }

            // Use contains to filter suggestions instead of default startsWith

            // Use contains to filter suggestions instead of default startsWith
            manager.commandSuggestionProcessor(
                FilteringCommandSuggestionProcessor(
                    FilteringCommandSuggestionProcessor.Filter.contains<CommandSender>(true).andTrimBeforeLastSpace()
                )
            )

            if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                this.manager.registerBrigadier()
            }

            if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                this.manager.registerAsynchronousCompletions()
            }



            val commandMetaFunction: Function<ParserParameters, CommandMeta> =
                Function<ParserParameters, CommandMeta> { p ->
                    CommandMeta.simple() // This will allow you to decorate commands with descriptions
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build()
                }
            this.annotationParser = AnnotationParser( /* Manager */
                this.manager,  /* Command sender type */
                CommandSender::class.java,  /* Mapper for command meta instances */
                commandMetaFunction
            )

        this.manager.parserRegistry().registerParserSupplier(
            TypeToken.get(LuaScript::class.java)
        ) { LoadedScriptParser(this.scriptManager) }

        this.manager.parserRegistry().registerParserSupplier(
            TypeToken.get(File::class.java)
        ) { AvailableScriptParser(this, this.scriptManager) }
    }
}
