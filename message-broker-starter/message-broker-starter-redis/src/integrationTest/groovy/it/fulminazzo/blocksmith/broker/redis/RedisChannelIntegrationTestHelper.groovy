package it.fulminazzo.blocksmith.broker.redis

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import org.slf4j.Logger

import java.util.function.Consumer

@SuppressWarnings('CloseWithoutCloseable')
class RedisChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper implements RedisIntegrationTest {
    private final RedisClient client
    protected final StatefulRedisConnection<String, String> connection
    protected final StatefulRedisPubSubConnection<String, String> pubSubConnection

    RedisChannelIntegrationTestHelper(final String channelName, final Logger logger) {
        super(channelName, logger)

        client = RedisClient.create("redis://$serverHost:$serverPort")
        connection = client.connect()
        pubSubConnection = client.connectPubSub()
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        connection.sync().publish(channelName, serializeMessage(message, conversationId))
    }

    @Override
    void close() throws IOException {
        pubSubConnection?.sync()?.unsubscribe(channelName)
        pubSubConnection?.close()
        connection?.close()
        client?.close()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        pubSubConnection.addListener(new RedisPubSubAdapter<String, String>() {

            @Override
            void message(final String channel, final String message) {
                if (channel == channelName) consumer.accept(message)
            }

        })
        pubSubConnection.sync().subscribe(channelName)
        return this
    }

}
