package it.fulminazzo.blocksmith.broker

import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Stepwise

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

@Stepwise
abstract class MessageChannelIntegrationTest extends Specification {
    protected static final Logger logger = LoggerFactory.getLogger(MessageChannelIntegrationTest)

    protected static final String CHANNEL_NAME = 'message-channel-integration-test'

    protected static final int SLEEP_TIME = 125

    protected MessageChannelIntegrationTestHelper helper
    protected MessageChannel channel

    void setupChannel() {
        helper = newTestHelper(CHANNEL_NAME).start()
        channel = initializeChannel()
    }

    void clearData() {
        channel?.close()
        helper?.close()
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

    def 'test that subscribe correctly handles message'() {
        given:
        def message = Messages.MESSAGE2

        and:
        def received = new AtomicReference<>()

        and:
        channel.subscribe(Message, (Consumer<Message>) (m -> received.set(m)))

        and:
        sleep(SLEEP_TIME)

        when:
        send(message, UUID.randomUUID())

        and:
        sleep(SLEEP_TIME)

        then:
        received.get() == message
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
        send(message, UUID.randomUUID())

        then:
        received.get() == null

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    abstract MessageChannel initializeChannel()

    abstract MessageChannelIntegrationTestHelper newTestHelper(final String channelName)

    /**
     * Checks if a message with the given id has been received.
     *
     * @param id the message it
     * @return {@code true} if it has been since the test started
     */
    protected boolean received(final @NotNull Long id) {
        return helper.received(id)
    }

    /**
     * Sends a new message.
     *
     * @param message the message
     */
    protected void send(final @NotNull Message message, final @NotNull UUID conversationId) {
        helper.send(message, conversationId)
    }

}
