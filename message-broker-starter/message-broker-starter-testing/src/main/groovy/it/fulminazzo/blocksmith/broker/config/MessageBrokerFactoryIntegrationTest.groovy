package it.fulminazzo.blocksmith.broker.config

import spock.lang.Specification

abstract class MessageBrokerFactoryIntegrationTest extends Specification {

    def 'test that build works'() {
        when:
        def broker = factory.build(config)

        then:
        broker != null

        cleanup:
        broker?.close()
    }

    protected abstract MessageBrokerFactory getFactory()

    protected abstract MessageBrokerConfig getConfig()

}
