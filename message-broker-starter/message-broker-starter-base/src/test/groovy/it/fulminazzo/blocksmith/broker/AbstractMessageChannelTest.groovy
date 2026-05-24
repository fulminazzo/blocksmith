package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.function.Function

class AbstractMessageChannelTest extends Specification {
    private static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    private final data = new Cat('Felix', 7, false)

    private final ScheduledExecutorService senderService = Executors.newSingleThreadScheduledExecutor()
    private final ScheduledExecutorService receiverService = Executors.newSingleThreadScheduledExecutor()

    private final AbstractMessageChannel sender = new MockMessageChannel(MAPPER, 'abstract-message-test-1', senderService)
    private final AbstractMessageChannel receiver = new MockMessageChannel(MAPPER, 'abstract-message-test-2', receiverService)

    void cleanup() {
        senderService.shutdown()
        receiverService.shutdown()
        sender.close()
        receiver.close()
    }

    def 'test that sendAndReceive works'() {
        given:
        final expected = new Cat('Sissy', 15, true)

        and:
        receiver.subscribe(expected.class, (Function<?, ?>) (d -> d == data ? expected : null))
        receiver.subscribe(String, (Function<?, ?>) (d -> null))

        when:
        def actual = sender.sendAndReceive(data, Cat, Duration.ofSeconds(10)).get()

        then:
        actual == expected
    }

    def 'test that sendAndReceiveRaw works'() {
        given:
        receiver.subscribeRaw((Function<String, String>) (r -> r == 'ping' ? 'pong' : null))

        when:
        def actual = sender.sendAndReceiveRaw('ping', Duration.ofSeconds(10)).get()

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
        given:
        final queue = MockMessageQueryEngine.getQueue(receiver.name)

        when:
        sender.send(data).join()

        then:
        !queue.empty

        and:
        def raw = queue.poll()
        def networkMessage = raw?.empty ? null : MAPPER.deserialize(raw, AbstractMessageChannel.NetworkMessage)
        networkMessage?.conversationId != null

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
        MockMessageQueryEngine.getQueue(receiver.name).add(MAPPER.serialize(data))

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
            senderService.shutdown()
            return expected
        }))

        when:
        def queue = MockMessageQueryEngine.getQueue(receiver.name)
        queue.add(MAPPER.serialize(data))

        and:
        sleep(250)

        then:
        def actualData = actual.get()
        actualData == data

        when:
        queue = MockMessageQueryEngine.getQueue(sender.name)

        then:
        !queue.empty
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
        !MockMessageQueryEngine.getQueue(receiver.name).empty
    }

    @SuppressWarnings('GroovyAccessibility')
    def 'test that handleMessage does not handle sent message once'() {
        given:
        final channel = Reflect.on(sender)
        final mapper = channel.get('mapper')

        and:
        def message = new AbstractMessageChannel.NetworkMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                'Hello, world'
        )
        final serialized = mapper.invoke('serialize', message).get()

        and:
        channel.get('sentMessages').get().add(message.id)

        and:
        def handled = new AtomicBoolean()
        channel.get('messageHandlers').get()[UUID.randomUUID()] = (Function<String, String>) (s -> {
            handled.set(true)
            return null
        })

        when:
        sender.handleMessage(serialized)

        then:
        !handled.get()

        when:
        sender.handleMessage(serialized)

        then:
        handled.get()
    }

    def 'test that dual close call does not throw'() {
        when:
        sender.close()

        then:
        noExceptionThrown()

        when:
        sender.close()

        then:
        noExceptionThrown()
    }

}
