package it.fulminazzo.blocksmith.broker.config

import it.fulminazzo.blocksmith.broker.MockMessageBroker
import spock.lang.Specification

class MessageBrokerFactoriesTest extends Specification {

    def 'test register-build cycle'() {
        given:
        final config = new MockMessageBrokerConfig()

        when:
        MessageBrokerFactories.registerFactory(
                config.class,
                new MockMessageBrokerFactory()
        )

        then:
        noExceptionThrown()

        when:
        def dataSource = MessageBrokerFactories.build(config)

        then:
        MockMessageBroker.isInstance(dataSource)

        cleanup:
        dataSource?.close()
    }

    def 'test that build of not found data source configuration type throws'() {
        when:
        MessageBrokerFactories.build(Mock(MessageBrokerConfig))

        then:
        thrown(IllegalArgumentException)
    }

}
