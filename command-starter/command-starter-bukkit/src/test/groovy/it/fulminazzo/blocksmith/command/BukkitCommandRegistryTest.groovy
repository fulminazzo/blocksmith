package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.reflect.Reflect
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.permissions.PermissionDefault
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

import java.lang.reflect.Parameter

@Slf4j
class BukkitCommandRegistryTest extends Specification {

    private ApplicationHandle application

    private BukkitCommandRegistry registry

    void setup() {
        MockBukkit.mock()

        application = Mock(ApplicationHandle)
        application.server() >> Bukkit.server
        application.logger() >> log
        application.messenger >> new Messenger(application)
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
                new PermissionInfo(null, 'help', Permission.Grant.ALL)
        )
        def plugin = new LiteralNode('plugin')
        plugin.commandInfo = new CommandInfo(
                'command.description.help.plugin',
                new PermissionInfo(null, 'help.plugin', Permission.Grant.OP)
        )
        node.addChild(plugin)
        def parameter = Mock(Parameter)
        parameter.type >> String
        def name = ArgumentNode.of('name', parameter, false)
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
        (helpPermission instanceof BukkitPermissionRegistry.BukkitPermission)
        helpPermission.name == 'help'
        helpPermission.default == PermissionDefault.TRUE
        helpPermission.children.keySet().toList() == ['help.plugin']

        and:
        def helpPluginPermission = pluginManager.getPermission('help.plugin')
        (helpPluginPermission instanceof BukkitPermissionRegistry.BukkitPermission)
        helpPluginPermission.name == 'help.plugin'
        helpPluginPermission.default == PermissionDefault.OP
        helpPluginPermission.children.keySet().toList() == []

        and:
        def previousCommands = registry.previousCommands
        previousCommands.size() == 1

        and:
        previousCommands['?'] == previousCommand

        and:
        def previousPermissions = registry.permissionRegistry.previousPermissions
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
        registry.permissionRegistry.previousPermissions['help'] = previousPermission

        and:
        def node = new LiteralNode('help', '?', 'showhelp', 'displayhelp')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo(null, 'help', Permission.Grant.ALL)
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
        pluginManager.addPermission(new BukkitPermissionRegistry.BukkitPermission(
                new PermissionInfo(null, 'help', Permission.Grant.ALL),
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
        def previousPermissions = registry.permissionRegistry.previousPermissions
        previousPermissions.isEmpty()
    }

    def 'test that onUnregister does not throw on unknown command'() {
        when:
        registry.onUnregister('unknown')

        then:
        noExceptionThrown()
    }

    def 'test that updateClientCommands works if syncCommands method is available'() {
        given:
        def server = Spy(CraftServer, additionalInterfaces: [Server])
        Reflect.on(server).set('map', new SimpleCommandMap(server))
        server.pluginManager >> Mock(PluginManager)

        and:
        def application = Mock(ApplicationHandle)
        application.server() >> server

        and:
        def registry = new BukkitCommandRegistry(application)

        when:
        registry.updateClientCommands()

        then:
        noExceptionThrown()

        and:
        1 * server.syncCommands()
    }

    def 'test that wrapSender instantiates a new Bukkit CommandSenderWrapper'() {
        given:
        def sender = Mock(CommandSender)

        when:
        def wrapped = registry.wrapSender(sender)

        then:
        (wrapped instanceof BukkitCommandSenderWrapper)
        wrapped.actualSender == sender
    }

    def 'test that sender type is correct'() {
        expect:
        registry.senderType == CommandSender
    }

    def 'test that getUsage of BukkitCommand returns correct usage'() {
        given:
        def registry = Mock(BukkitCommandRegistry)

        and:
        def node = new LiteralNode('hello')
        node.commandInfo = new CommandInfo(
                'hello.description',
                new PermissionInfo(null, 'hello.permission', Permission.Grant.NONE)
        )

        and:
        def command = new BukkitCommandRegistry.BukkitCommand(registry, 'hello', node)

        when:
        def usage = command.usage

        then:
        usage == '§c/hello'
    }

    def 'test that BukkitCommand delegates #method to #expected'() {
        given:
        def registry = Mock(BukkitCommandRegistry)

        and:
        def node = new LiteralNode('test')
        node.commandInfo = new CommandInfo(
                'test.description',
                new PermissionInfo(null, 'test.permission', Permission.Grant.NONE)
        )
        def sender = Mock(CommandSender)
        def args = ['first', 'second', 'third'].toArray(String[]::new)

        and:
        def command = new BukkitCommandRegistry.BukkitCommand(registry, 'test', node)

        when:
        command."$method"(sender, 'test', args)

        then:
        1 * registry."$expected"(node, sender, 'test', args)

        where:
        method        || expected
        'execute'     || 'execute'
        'tabComplete' || 'tabComplete'
    }

    private static class CraftServer {
        private SimpleCommandMap map

        @SuppressWarnings('unused')
        void syncCommands() {

        }

    }

}
