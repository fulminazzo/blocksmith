package it.fulminazzo.blocksmith.command

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.message.Messenger
import spock.lang.Specification

@Slf4j
class CommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry with no service throws'() {
        when:
        CommandRegistryFactory.newCommandRegistry(new Messenger(log), log)

        then:
        thrown(IllegalStateException)
    }

}
