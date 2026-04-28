package it.fulminazzo.blocksmith.broker

import org.jetbrains.annotations.NotNull
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

abstract class MessageChannelTest extends Specification {
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
        sleep(125)

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
        sleep(125)

        when:
        send(message)

        then:
        received.get() == message

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
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
