package it.fulminazzo.blocksmith.broker.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannel
import it.fulminazzo.blocksmith.broker.MessageChannelTest
import it.fulminazzo.blocksmith.broker.Messages
import it.fulminazzo.blocksmith.data.mapper.Mapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import org.jetbrains.annotations.NotNull
import redis.embedded.RedisServer

class RedisMessageChannelTest extends MessageChannelTest {
    private static final String channelName = 'redis-message-channel'
    private static final Mapper mapper = MapperFormat.JSON.newMapper()
    private static final int serverPort = 16389

    private static RedisServer server
    private static RedisClient client
    private static StatefulRedisConnection<String, String> connection
    private static StatefulRedisPubSubConnection<String, String> pubSubConnection

    private static final Queue<Message> receivedMessages = new LinkedList<>()

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()

        client = RedisClient.create("redis://localhost:$serverPort")
        connection = client.connect()
        pubSubConnection = client.connectPubSub()

        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {

            @Override
            void message(final String channel, final String message) {
                if (channel == channelName)
                    receivedMessages.add(mapper.deserialize(message, Message))
            }

        })
        pubSubConnection.sync().subscribe(channelName)
    }

    void setup() {
        setupChannel()
    }

    void cleanup() {
        clearData()
        receivedMessages.clear()
    }

    void cleanupSpec() {
        pubSubConnection?.close()
        client?.shutdown()
        server?.stop()
    }

    def 'test that server is online'() {
        expect:
        server.active
    }

    def 'test that sending on server works'() {
        when:
        send(message)

        and:
        sleep(125)

        then:
        received(message.id)

        where:
        message << [Messages.MESSAGE1, Messages.MESSAGE2]
    }

    @Override
    MessageChannel initializeChannel() {
        return new RedisMessageChannel(
                new RedisMessageQueryEngine(
                        connection,
                        pubSubConnection,
                        channelName
                ),
                mapper
        )
    }

    @Override
    boolean received(final @NotNull Long id) {
        return receivedMessages.any { it.id == id }
    }

    @Override
    void send(final @NotNull Message message) {
        connection.sync().publish(channelName, mapper.serialize(message))
    }

}
