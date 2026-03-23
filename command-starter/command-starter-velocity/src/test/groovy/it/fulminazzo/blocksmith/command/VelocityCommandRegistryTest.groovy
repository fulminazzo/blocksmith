package it.fulminazzo.blocksmith.command


import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.proxy.ProxyServer
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.BlocksmithApplication
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.CommandInfo
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.PermissionInfo
import it.fulminazzo.blocksmith.message.Messenger
import spock.lang.Specification

@Slf4j
class VelocityCommandRegistryTest extends Specification {

    private BlocksmithApplication application

    private VelocityCommandRegistry registry

    private CommandManager commandManager

    void setup() {
        def mockMeta = Mock(CommandMeta)
        def mockBuilder = Mock(CommandMeta.Builder)

        mockBuilder.aliases(_ as String[]) >> mockBuilder
        mockBuilder.hint(_) >> mockBuilder
        mockBuilder.build() >> mockMeta

        commandManager = Mock(CommandManager)
        commandManager.metaBuilder(_ as String) >> mockBuilder

        def proxy = Mock(ProxyServer)
        proxy.commandManager >> commandManager

        application = Mock(BlocksmithApplication)
        application.server >> proxy
        application.log >> log
        application.messenger >> new Messenger(log)
        application.name >> 'blocksmith'

        registry = new VelocityCommandRegistry(application)
    }

    def 'test that onRegister correctly registers new command and aliases'() {
        given:
        def node = new LiteralNode('help', '?')
        node.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo('help', Permission.Grant.ALL)
        )

        when:
        registry.onRegister('help', node)

        then:
        noExceptionThrown()

        and:
        1 * commandManager.register(_ as CommandMeta, _ as VelocityCommandRegistry.VelocityCommand)

        and:
        def registeredAliases = registry.registeredAliases
        def aliases = registeredAliases['help']
        aliases != null

        and:
        aliases == ['?']
    }

    def 'test that onUnregister removes commands'() {
        given:
        registry.registeredAliases['help'] = ['?']

        when:
        registry.onUnregister('help')

        then:
        noExceptionThrown()

        and:
        1 * commandManager.unregister('help')

        and:
        def aliases = registry.registeredAliases
        aliases['help'] == null

        and:
        1 * commandManager.unregister('?')
    }

}
