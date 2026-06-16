package it.fulminazzo.blocksmith.broker.config

import spock.lang.Specification

class MessageBrokerTypeTest extends Specification {

    def 'test that getConfigClass throws for type #messageBrokerType'() {
        given:
        def name = messageBrokerType.name().toLowerCase().capitalize()
        if (messageBrokerType == MessageBrokerType.RABBITMQ) name = 'RabbitMQ'

        when:
        messageBrokerType.configClass

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${MessageBrokerConfig.simpleName} for ${name}. " +
                "Please check that the module it.fulminazzo.blocksmith:message-broker-starter-${name.toLowerCase()} is correctly installed."

        where:
        messageBrokerType << MessageBrokerType.values()
    }
    
}
