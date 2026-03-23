package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.BlocksmithApplication
import it.fulminazzo.blocksmith.message.Messenger
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import spock.lang.Specification

@Slf4j
class BungeeCommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(mockApplication())

        then:
        (registry instanceof BungeeCommandRegistry)
    }

    private BlocksmithApplication mockApplication() {
        BlocksmithApplication application = Mock(Plugin, additionalInterfaces: [BlocksmithApplication]) as BlocksmithApplication
        application.messenger >> new Messenger(log)
        application.log >> log
        application.name >> 'blocksmith'
        application.server >> Mock(ProxyServer)
        return application
    }

}
