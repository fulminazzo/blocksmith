package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

abstract class MessageChannelTest extends Specification {
    protected static final Logger logger = LoggerFactory.getLogger(MessageChannelTest)

    protected static final int SLEEP_TIME = 125

    protected static final Mapper MAPPER = MapperFormat.JSON.newMapper()

    protected MessageChannel channel

    void setupChannel() {
        channel = initializeChannel()
    }

    void clearData() {
        channel.close()
    }

    def 'test that sending of MESSAGE1 returns MESSAGE2'() {
        when:
        def actual = channel.sendAndReceive(
                Messages.MESSAGE1,
                Message,
                1_000
        ).join()

        then:
        actual == Messages.MESSAGE2
    }

    def 'test that send correctly sends message #message'() {
        when:
        channel.send(message)

        and:
        sleep(SLEEP_TIME)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    def 'test that subscribe correctly handles message #message'() {
        given:
        def received = new AtomicReference<>()

        and:
        channel.subscribe(Message, (Consumer<Message>) (m -> received.set(m)))

        and:
        sleep(SLEEP_TIME)

        when:
        send(message)

        and:
        sleep(SLEEP_TIME)

        then:
        received.get() == message

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    def 'test that unsubscribe correctly removes handler'() {
        given:
        def received = new AtomicReference<>()

        and:
        def id = channel.subscribe(Message, (Consumer<Message>) (m -> received.set(m)))

        and:
        sleep(SLEEP_TIME)

        and:
        channel.unsubscribe(id)

        and:
        sleep(SLEEP_TIME)

        when:
        send(message)

        then:
        received.get() == null

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    protected String serializeMessage(final @NotNull Message message) {
        return MAPPER.serialize(new AbstractMessageChannel.NetworkMessage(
                UUID.randomUUID(),
                MAPPER.serialize(message)
        ))
    }

    protected Message deserializeMessage(final @NotNull String payload) {
        return MAPPER.deserialize(
                MAPPER.deserialize(
                        payload,
                        AbstractMessageChannel.NetworkMessage
                ).message,
                Message
        )
    }

    abstract MessageChannel initializeChannel()

    /**
     * Checks if a message with the given id has been received.
     *
     * @param id the message it
     * @return {@code true} if it has been since the test started
     */
    abstract boolean received(final @NotNull Long id)

    /**
     * Sends a new message.
     *
     * @param message the message
     */
    abstract void send(final @NotNull Message message)

}
