package it.fulminazzo.blocksmith.broker.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelTest
import it.fulminazzo.blocksmith.broker.Messages
import org.jetbrains.annotations.NotNull
import redis.embedded.RedisServer

class RedisMessageChannelTest extends MessageChannelTest {
    private static final String channelName = 'redis-message-channel'
    private static final int serverPort = 16389

    private static RedisServer server
    private static RedisClient client
    private static StatefulRedisConnection<String, String> connection

    private static final Queue<Message> receivedMessages = new LinkedList<>()

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()
    }

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
        receivedMessages.clear()
    }

    void cleanupSpec() {
        client?.shutdown()
        server?.stop()
    }

    def 'test that server is online'() {
        expect:
        server.active
    }

    def 'test that sending on server works'() {
        when:
        send(message, UUID.randomUUID())

        and:
        sleep(SLEEP_TIME)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        def pubSubConnection = client.connectPubSub()
        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {

            @Override
            void message(final String channel, final String message) {
                if (channel == channelName) {
                    logger.debug("Received on channel ($channel) raw: $message")
                    def pair = deserializeMessage(message)
                    def msg = pair.first
                    logger.info("Received on channel ($channel) message with id=$msg.id")
                    receivedMessages.add(msg)
                    if (msg == Messages.MESSAGE1)
                        send(Messages.MESSAGE2, pair.second)
                }
            }

        })
        pubSubConnection.sync().subscribe(channelName)

        return new RedisMessageChannel(
                new RedisMessageQueryEngine(channelName,
                        connection,
                        pubSubConnection

                ),
                MAPPER
        )
    }

    @Override
    boolean received(final @NotNull Long id) {
        return receivedMessages.any { it.id == id }
    }

    @Override
    void send(final @NotNull Message message, final @NotNull UUID conversationId) {
        connection.sync().publish(channelName, serializeMessage(message, conversationId))
    }

}
