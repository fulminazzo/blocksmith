package it.fulminazzo.blocksmith.command

import com.velocitypowered.api.proxy.ProxyServer
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.BlocksmithApplication
import it.fulminazzo.blocksmith.message.Messenger
import spock.lang.Specification

@Slf4j
class VelocityCommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(mockApplication())

        then:
        (registry instanceof VelocityCommandRegistry)
    }

    private BlocksmithApplication mockApplication() {
        BlocksmithApplication application = Mock(BlocksmithApplication)
        application.messenger >> new Messenger(log)
        application.log >> log
        application.name >> 'blocksmith'
        application.server >> Mock(ProxyServer)
        return application
    }

}
