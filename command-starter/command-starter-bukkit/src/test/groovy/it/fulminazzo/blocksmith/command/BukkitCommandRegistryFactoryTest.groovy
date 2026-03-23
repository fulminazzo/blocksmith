package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.message.Messenger
import spock.lang.Specification

@Slf4j
class BukkitCommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry returns BukkitCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(Mock(Messenger), log, 'prefix')

        then:
        (registry instanceof BukkitCommandRegistry)
    }

}
