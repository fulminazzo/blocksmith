package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.function.Function

class AbstractMessageChannelTest extends Specification {
    private static final String NAME = 'abstract-message-test'
    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    private final data = new Cat('Felix', 7, false)

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()

    private AbstractMessageChannel sender = new MockMessageChannel(MAPPER, NAME)
    private AbstractMessageChannel receiver = new MockMessageChannel(MAPPER, NAME, executorService)

    void cleanup() {
        executorService.shutdown()
    }

    def 'test that send correctly sends serialized payload'() {
        given:
        final name = "${NAME}1"
        def sender = new MockMessageChannel(MAPPER, name)

        when:
        sender.send(data).join()

        then:
        def queue = MockMessageChannel.getQueue(name)
        !queue.isEmpty()

        and:
        def raw = queue.poll()
        def networkMessage = MAPPER.deserialize(raw, AbstractMessageChannel.NetworkMessage)
        networkMessage.conversationId != null

        and:
        def rawPayload = networkMessage.message
        def actual = MAPPER.deserialize(rawPayload, data.class)
        actual == data
    }

    def 'test that subscribe correctly intercepts and passes deserialized payload'() {
        given:
        def actual = new AtomicReference<>()
        def raw = new AtomicReference<>()

        and:
        receiver.subscribe(data.class, (Consumer<?>) (d -> actual.set(d)))
        receiver.subscribeRaw((Consumer<String>) (r -> raw.set(r)))

        when:
        MockMessageChannel.getQueue(NAME).add(MAPPER.serialize(data))

        and:
        sleep(250)

        then:
        def actualData = actual.get()
        actualData == data

        and:
        def actualRaw = raw.get()
        MAPPER.deserialize(actualRaw, data.class) == data
    }

    def 'test that subscribe correctly intercepts, passes deserialized payload and sends response if not null'() {
        given:
        def actual = new AtomicReference<>()

        and:
        def expected = new Cat('Cindy', 5, true)

        and:
        receiver.subscribe(data.class, (Function<?, ?>) (d -> {
            actual.set(d)
            executorService.shutdown()
            return expected
        }))

        when:
        def queue = MockMessageChannel.getQueue(NAME)
        queue.add(MAPPER.serialize(data))

        and:
        sleep(250)

        then:
        def actualData = actual.get()
        actualData == data

        and:
        !queue.isEmpty()
        def raw = queue.poll()
        def networkMessage = MAPPER.deserialize(raw, AbstractMessageChannel.NetworkMessage)
        networkMessage.conversationId != null

        and:
        def rawPayload = networkMessage.message
        def response = MAPPER.deserialize(rawPayload, expected.class)
        response == expected
    }

    def 'test that unsubscribe correctly removes handler'() {
        given:
        def actual = new AtomicReference<>()

        and:
        def id = receiver.subscribeRaw((Consumer<String>) (r -> actual.set(r)))

        when:
        receiver.unsubscribe(id)

        and:
        sender.sendRaw('Hello, world!')

        then:
        actual.get() == null
        !MockMessageChannel.getQueue(NAME).isEmpty()
    }

}
