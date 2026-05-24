package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import spock.lang.Specification

class AbstractMessageBrokerBuilderTest extends Specification {

    def 'test creation of mock MessageBrokerBuilder'() {
        given:
        def mapper = Mock(Mapper)

        when:
        def broker = new MockMessageBrokerBuilder().mapper(mapper).build()

        then:
        broker != null
        broker.mapper == mapper
    }

}
