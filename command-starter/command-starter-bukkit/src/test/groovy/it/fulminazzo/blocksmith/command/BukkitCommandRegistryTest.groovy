package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.CommandInfo
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.permissions.PermissionDefault
import spock.lang.Specification

@Slf4j
class BukkitCommandRegistryTest extends Specification {

    private ApplicationHandle application

    private BukkitCommandRegistry registry

    void setup() {
        MockBukkit.mock()

        application = Mock(ApplicationHandle)
        application.server >> Bukkit.server
        application.log >> log
        application.messenger >> new Messenger(log)
        application.name >> 'blocksmith'

        registry = new BukkitCommandRegistry(application)
    }

    void cleanup() {
        MockBukkit.unmock()
    }

    def 'test that onRegister correctly registers new command and aliases'() {
        given:
        def previousCommand = Mock(Command)
        registry.knownCommands.put('?', previousCommand)

        and:
        def pluginManager = Bukkit.server.pluginManager
        def previousPermission = new org.bukkit.permissions.Permission(
                'help',
                PermissionDefault.TRUE
        )
        pluginManager.addPermission(previousPermission)

        and:
        def node = new LiteralNode('help', '?')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo('help', Permission.Grant.ALL)
        )
        def plugin = new LiteralNode('plugin')
        plugin.commandInfo = new CommandInfo(
                'command.description.help.plugin',
                new PermissionInfo('help.plugin', Permission.Grant.OP)
        )
        node.addChild(plugin)
        def name = new ArgumentNode('name', String, false)
        plugin.addChild(name)

        when:
        registry.onRegister('help', node)

        then:
        noExceptionThrown()

        and:
        def knownCommands = registry.knownCommands
        (knownCommands['help'] instanceof BukkitCommandRegistry.BukkitCommand)
        (knownCommands['?'] instanceof BukkitCommandRegistry.BukkitCommand)
        (knownCommands["${application.name}:help"] instanceof BukkitCommandRegistry.BukkitCommand)
        (knownCommands["${application.name}:?"] instanceof BukkitCommandRegistry.BukkitCommand)

        and:
        def helpPermission = pluginManager.getPermission('help')
        helpPermission != previousPermission
        (helpPermission instanceof BukkitCommandRegistry.BukkitPermission)
        helpPermission.name == 'help'
        helpPermission.default == PermissionDefault.TRUE
        helpPermission.children.keySet().toList() == ['help.plugin']

        and:
        def helpPluginPermission = pluginManager.getPermission('help.plugin')
        (helpPluginPermission instanceof BukkitCommandRegistry.BukkitPermission)
        helpPluginPermission.name == 'help.plugin'
        helpPluginPermission.default == PermissionDefault.OP
        helpPluginPermission.children.keySet().toList() == []

        and:
        def previousCommands = registry.previousCommands
        previousCommands.size() == 1

        and:
        previousCommands['?'] == previousCommand

        and:
        def previousPermissions = registry.previousPermissions
        previousPermissions.size() == 1

        and:
        previousPermissions['help'] == previousPermission
    }

    def 'test that onUnregister removes commands and restores previous commands'() {
        given:
        def previousCommand = Mock(Command)
        registry.previousCommands['?'] = previousCommand
        registry.previousCommands['showhelp'] = previousCommand

        and:
        def previousPermission = new org.bukkit.permissions.Permission(
                'help',
                PermissionDefault.TRUE
        )
        registry.previousPermissions['help'] = previousPermission

        and:
        def node = new LiteralNode('help', '?', 'showhelp', 'displayhelp')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo('help', Permission.Grant.ALL)
        )
        def command = new BukkitCommandRegistry.BukkitCommand(registry, 'help', node)
        command.permission = 'help'

        and:
        registry.knownCommands['help'] = command
        registry.knownCommands['?'] = command
        registry.knownCommands["${application.name}:help"] = command
        registry.knownCommands["${application.name}:?"] = command
        registry.knownCommands['showhelp'] = Mock(Command)

        and:
        def pluginManager = Bukkit.server.pluginManager
        pluginManager.addPermission(new BukkitCommandRegistry.BukkitPermission(
                new PermissionInfo('help', Permission.Grant.ALL),
                ['help.plugin']
        ))
        def otherPermission = new org.bukkit.permissions.Permission('help.plugin')
        pluginManager.addPermission(otherPermission)

        when:
        registry.onUnregister('help')

        then:
        noExceptionThrown()

        and:
        def knownCommands = registry.knownCommands
        knownCommands['help'] == null
        knownCommands['?'] == previousCommand
        knownCommands["${application.name}help"] == null
        knownCommands["${application.name}?"] == null

        and:
        def helpPermission = pluginManager.getPermission('help')
        helpPermission == previousPermission

        and:
        def helpPluginPermission = pluginManager.getPermission('help.plugin')
        helpPluginPermission == otherPermission

        and:
        def previousCommands = registry.previousCommands
        previousCommands.isEmpty()

        and:
        def previousPermissions = registry.previousPermissions
        previousPermissions.isEmpty()
    }

    def 'test that onUnregister does not throw on unknown command'() {
        when:
        registry.onUnregister('unknown')

        then:
        noExceptionThrown()
    }

}
