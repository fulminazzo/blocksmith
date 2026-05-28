package it.fulminazzo.blocksmith.broker.kafka

import spock.lang.Specification

class KafkaMessageBrokerBuilderTest extends Specification {

    def 'test that build throws if no server is provided'() {
        given:
        def builder = new KafkaMessageBrokerBuilder()

        when:
        builder.build()

        then:
        thrown(IllegalStateException)
    }

    def 'test that build does not throw if executor is not provided'() {
        given:
        def builder = new KafkaMessageBrokerBuilder()
                .bootstrapServer('0.0.0.0', 9002)

        when:
        def broker = builder.build()

        then:
        broker != null

        cleanup:
        broker.close()
    }

}
