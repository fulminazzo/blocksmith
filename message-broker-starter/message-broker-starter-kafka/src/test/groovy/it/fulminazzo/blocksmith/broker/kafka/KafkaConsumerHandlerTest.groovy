package it.fulminazzo.blocksmith.broker.kafka

import org.apache.kafka.common.serialization.StringDeserializer
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class KafkaConsumerHandlerTest extends Specification {

    def 'test that initialization throws if it could not get an assignment in the given interval'() {
        given:
        def properties = new Properties()
        properties['bootstrap.servers'] = 'localhost:12345'
        properties['key.deserializer'] = StringDeserializer.canonicalName
        properties['value.deserializer'] = StringDeserializer.canonicalName
        properties['group.id'] = 'test-group'

        when:
        new KafkaConsumerHandler(
                properties,
                ['test-topic'],
                500,
                1000
        ) {

            @Override
            protected void handle(final @NotNull Object key, final @NotNull Object value) {
                // do nothing
            }

        }

        then:
        thrown(KafkaMessageBrokerException)
    }

}
