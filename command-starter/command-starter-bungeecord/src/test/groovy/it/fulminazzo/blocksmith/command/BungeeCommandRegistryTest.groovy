package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.PluginManager
import spock.lang.Specification

@Slf4j
class BungeeCommandRegistryTest extends Specification {

    private ApplicationHandle application

    private BungeeCommandRegistry registry

    private PluginManager pluginManager

    void setup() {
        pluginManager = Mock(PluginManager)

        def proxy = Mock(ProxyServer)
        proxy.pluginManager >> pluginManager

        application = Mock(Plugin, additionalInterfaces: [ApplicationHandle]) as ApplicationHandle
        application.proxy >> proxy
        application.server() >> {
            return application.proxy
        }
        application.logger() >> log
        application.messenger >> new Messenger(application)
        application.name >> 'blocksmith'

        registry = new BungeeCommandRegistry(application)
    }

    def 'test that onRegister correctly registers new command and aliases'() {
        given:
        def node = new LiteralNode('help', '?')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo(null, 'help', Permission.Grant.ALL)
        )

        when:
        registry.onRegister('help', node)

        then:
        noExceptionThrown()

        and:
        1 * pluginManager.registerCommand(application, _ as BungeeCommandRegistry.BungeeCommand)

        and:
        def registeredCommands = registry.registeredCommands
        def command = registeredCommands['help']
        command != null

        and:
        command.aliases.toList() == ['?']
    }

    def 'test that onUnregister removes commands'() {
        given:
        def command = Mock(BungeeCommandRegistry.BungeeCommand)
        registry.registeredCommands['help'] = command

        when:
        registry.onUnregister('help')

        then:
        noExceptionThrown()

        and:
        1 * pluginManager.unregisterCommand(command)

        and:
        def registeredCommands = registry.registeredCommands
        registeredCommands['help'] == null
    }

    def 'test that wrapSender instantiates a new Bungee CommandSenderWrapper'() {
        given:
        def sender = Mock(CommandSender)

        when:
        def wrapped = registry.wrapSender(sender)

        then:
        (wrapped instanceof BungeeCommandSenderWrapper)
        wrapped.actualSender == sender
    }

    def 'test that sender type is correct'() {
        expect:
        registry.senderType == CommandSender
    }

    def 'test that BungeeCommand delegates #method to #expected'() {
        given:
        def registry = Mock(BungeeCommandRegistry)

        and:
        def node = new LiteralNode('test')
        node.commandInfo = new CommandInfo(
                'test.description',
                new PermissionInfo(null, 'test.permission', Permission.Grant.NONE)
        )
        def sender = Mock(CommandSender)
        def args = ['first', 'second', 'third'].toArray(String[]::new)

        and:
        def command = new BungeeCommandRegistry.BungeeCommand(registry, 'test', node)

        when:
        command."$method"(sender, args)

        then:
        1 * registry."$expected"(node, sender, 'test', args)

        where:
        method          || expected
        'execute'       || 'execute'
        'onTabComplete' || 'tabComplete'
    }

}
