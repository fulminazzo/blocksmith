package it.fulminazzo.blocksmith.broker.rabbitmq

import com.rabbitmq.client.*
import it.fulminazzo.blocksmith.broker.Message
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import org.slf4j.Logger
import org.testcontainers.containers.RabbitMQContainer

import java.nio.charset.StandardCharsets
import java.util.function.Consumer

@SuppressWarnings('CloseWithoutCloseable')
class RabbitMQChannelIntegrationTestHelper extends MessageChannelIntegrationTestHelper {
    static final String QUEUE_NAME = 'test-queue'

    private static final RabbitMQContainer RABBIT_MQ_SERVER = new RabbitMQContainer('rabbitmq:4.3.0-alpine')
            .withReuse(true)

    private static int consumerCount = 0

    private final Connection connection
    protected final Channel channel

    RabbitMQChannelIntegrationTestHelper(final String channelName, final Logger logger) {
        super(channelName, logger)

        final factory = new ConnectionFactory()
        factory.host = serverHost
        factory.port = serverPort

        connection = factory.newConnection()
        channel = connection.createChannel()
    }

    @Override
    void send(final Message message, final UUID conversationId) {
        final String channelName
        final String subchannelName
        if (super.channelName.contains(':')) {
            def split = super.channelName.split(':')
            channelName = split[0]
            subchannelName = split[1]
        } else {
            channelName = super.channelName
            subchannelName = ''
        }
        channel.basicPublish(
                channelName,
                subchannelName,
                null,
                serializeMessage(message, conversationId).bytes
        )
    }

    @Override
    void close() throws IOException {
        channel?.close()
        connection?.close()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        channel.queueDeclare(QUEUE_NAME, true, false, false, null)
        channel.basicConsume(
                QUEUE_NAME,
                false,
                "test-consumer-${consumerCount++}",
                new DefaultConsumer(channel) {

                    @Override
                    void handleDelivery(
                            final String consumerTag,
                            final Envelope envelope,
                            final AMQP.BasicProperties properties,
                            final byte[] body
                    ) throws IOException {
                        consumer.accept(new String(body, StandardCharsets.UTF_8))
                    }

                }
        )
        return this
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods')
    // enforce our ordering
    static String getServerHost() {
        return container.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods')
    // enforce our ordering
    static int getServerPort() {
        return container.amqpPort
    }

    protected static RabbitMQContainer getContainer() {
        if (!RABBIT_MQ_SERVER.created) RABBIT_MQ_SERVER.start()
        return RABBIT_MQ_SERVER
    }

}
