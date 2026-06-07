package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class AbstractMessageBrokerTest extends Specification {
    private static final ScheduledExecutorService SERVICE = Executors.newSingleThreadScheduledExecutor()
    private static final Mapper MAPPER = MapperFormat.SERIALIZABLE.newMapper()

    private final AbstractMessageBroker<? extends MessageChannelSettings> broker = new MockMessageBroker(MapperFormat.SERIALIZABLE.newMapper())

    void cleanupSpec() {
        SERVICE.close()
    }

    void setup() {
        MockMessageQueryEngine.clear()
    }

    def 'test that registerChannel registers new channel and removes closed ones'() {
        given:
        def first = new MockMessageChannel(MAPPER, 'first', SERVICE)
        def second = new MockMessageChannel(MAPPER, 'second', SERVICE)

        and:
        List<MessageChannel> channels = registeredChannels

        when:
        broker.registerChannel(first)

        then:
        first in channels

        when:
        first.close()

        and:
        broker.registerChannel(second)

        then:
        second in channels
        first !in channels
    }

    def 'test that close closes all registered channels'() {
        given:
        def first = new MockMessageChannel(MAPPER, 'first', SERVICE)
        def second = new MockMessageChannel(MAPPER, 'second', SERVICE)

        and:
        List<MessageChannel> channels = registeredChannels

        and:
        channels.add(first)
        channels.add(second)

        when:
        broker.close()

        then:
        channels.empty
        first.closed
        second.closed
    }

    protected List<MessageChannel> getRegisteredChannels() {
        return Reflect.on(broker)['registeredChannels'].get()
    }

}
