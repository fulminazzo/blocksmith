package it.fulminazzo.blocksmith.command

import spock.lang.Specification

class CommandRegistryFactoryTest extends Specification {

    def 'test that newCommandRegistry with no service throws'() {
        when:
        CommandRegistryFactory.newCommandRegistry('blocksmith')

        then:
        thrown(IllegalStateException)
    }

}
