package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.BlocksmithApplication
import spock.lang.Specification

@Slf4j
class BungeeCommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(Mock(BlocksmithApplication))

        then:
        (registry instanceof BungeeCommandRegistry)
    }

}
