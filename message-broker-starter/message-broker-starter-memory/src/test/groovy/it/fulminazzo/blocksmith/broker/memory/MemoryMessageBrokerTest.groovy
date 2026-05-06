package it.fulminazzo.blocksmith.broker.memory

import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

class MemoryMessageBrokerTest extends Specification {

    def 'test broker life cycle'() {
        given:
        def messageBroker = MemoryMessageBroker.create(MapperFormat.JSON.newMapper())

        when:
        def directChannel = messageBroker.newChannel(
                new MemoryMessageChannelSettings()
                        .withChannelName('main')
                        .direct('sub')
        )

        then:
        directChannel != null

        when:
        def broadcastChannel = messageBroker.newChannel(
                new MemoryMessageChannelSettings()
                        .withChannelName('main')
                        .broadcast()
        )

        then:
        broadcastChannel != null

        when:
        messageBroker.close()

        then:
        noExceptionThrown()
    }

}
