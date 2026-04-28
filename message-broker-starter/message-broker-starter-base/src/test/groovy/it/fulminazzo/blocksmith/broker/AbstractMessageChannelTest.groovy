package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.function.Function

class AbstractMessageChannelTest extends Specification {
    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    private final data = new Cat('Felix', 7, false)

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()

    private AbstractMessageChannel sender = new MockMessageChannel(MAPPER, "abstract-message-test-1", executorService)
    private AbstractMessageChannel receiver = new MockMessageChannel(MAPPER, "abstract-message-test-2", executorService)

    void cleanup() {
        executorService.shutdown()
    }

    def 'test that sendAndReceive works'() {
        given:
        final expected = new Cat('Sissy', 15, true)

        and:
        receiver.subscribe(expected.class, (Function<?, ?>) (d -> d == data ? expected : null))

        when:
        def actual = sender.sendAndReceive(data, Cat, Duration.ofSeconds(1)).get()

        then:
        actual == expected
    }

    def 'test that sendAndReceiveRaw works'() {
        given:
        receiver.subscribeRaw((MessageHandler) (r -> r == 'ping' ? 'pong' : null))

        when:
        def actual = sender.sendAndReceiveRaw('ping', Duration.ofSeconds(1)).get()

        then:
        actual == 'pong'
    }

    def 'test that sendAndReceiveRaw throws Timeout if nobody is subscribed'() {
        when:
        sender.sendAndReceiveRaw('ping', Duration.ofMillis(125)).get()

        then:
        def e = thrown(ExecutionException)
        (e.cause instanceof TimeoutException)
    }

    def 'test that send correctly sends serialized payload'() {
        when:
        sender.send(data).join()

        then:
        def queue = MockMessageChannel.getQueue(receiver.name)
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
        MockMessageChannel.getQueue(receiver.name).add(MAPPER.serialize(data))

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
        def queue = MockMessageChannel.getQueue(receiver.name)
        queue.add(MAPPER.serialize(data))

        and:
        sleep(250)

        then:
        def actualData = actual.get()
        actualData == data

        when:
        queue = MockMessageChannel.getQueue(sender.name)

        then:
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
        !MockMessageChannel.getQueue(receiver.name).isEmpty()
    }

}
