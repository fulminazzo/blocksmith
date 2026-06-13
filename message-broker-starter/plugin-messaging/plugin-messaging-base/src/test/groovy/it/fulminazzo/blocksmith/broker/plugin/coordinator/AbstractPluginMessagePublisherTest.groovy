package it.fulminazzo.blocksmith.broker.plugin.coordinator

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class AbstractPluginMessagePublisherTest extends Specification {

    def 'test that republishFailedMessages adds failed messages a second time if publication fails'() {
        given:
        def validator = a -> new String(a[0]) == 'valid'

        and:
        def publisher = Spy(AbstractPluginMessagePublisher)
        publisher.publishImpl(_, _) >> { a -> return validator(a) }

        and:
        final failedMessages = Reflect.on(publisher).get('failedMessages').get()

        when:
        publisher.publish('valid', 'Hello'.bytes)
        publisher.publish('valid', 'world'.bytes)
        publisher.publish('invalid', 'valid:Hello'.bytes)
        publisher.publish('invalid', 'invalid:world'.bytes)

        then:
        failedMessages.size() == 1
        failedMessages['invalid'].size() == 2

        when:
        validator = a -> new String(a[1]).startsWith('valid:')

        and:
        publisher.republishFailedMessages()

        then:
        failedMessages.size() == 1
        failedMessages['invalid'].size() == 1
    }


}
