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

    private static final RabbitMQContainer RABBIT_MQ_SERVER = new RabbitMQContainer('rabbitmq:4.3.0-management-alpine')
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

    /**
     * Sets up the internal exchange and queue.
     *
     * @return the exchange, routing key and queue
     */
    protected Tuple<String> setupExchangeAndQueue() {
        final queueName = QUEUE_NAME
        def (baseChannelName, subchannelName) = getChannelNames(channelName)

        channel.exchangeDeclare(baseChannelName, subchannelName.empty ? 'fanout' : 'direct', true)

        channel.queueDeclare(queueName, true, false, false, null)
        channel.queueBind(queueName, baseChannelName, subchannelName)

        return [baseChannelName, subchannelName, queueName]
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    @Override
    void send(final Message message, final UUID conversationId) {
        def (exchange, routingKey) = setupExchangeAndQueue()
        channel.basicPublish(
                exchange,
                routingKey,
                null,
                serializeMessage(message, conversationId).bytes
        )
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    @Override
    void close() throws IOException {
        if (channel.open) channel?.close()
        connection?.close()
        super.close()
    }

    @Override
    protected MessageChannelIntegrationTestHelper start(
            final String channelName,
            final Logger logger,
            final Consumer<String> consumer
    ) {
        def (_, _, queueName) = setupExchangeAndQueue()
        channel.basicConsume(
                queueName,
                true,
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

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static Tuple<String> getChannelNames(final String channelName) {
        final String baseChannelName
        final String subchannelName
        if (channelName.contains(':')) {
            def split = channelName.split(':')
            baseChannelName = split[0]
            subchannelName = split[1]
        } else {
            baseChannelName = channelName
            subchannelName = ''
        }
        [baseChannelName, subchannelName]
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static String getServerHost() {
        return container.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static int getServerPort() {
        return container.amqpPort
    }

    protected static RabbitMQContainer getContainer() {
        if (!RABBIT_MQ_SERVER.created) RABBIT_MQ_SERVER.start()
        return RABBIT_MQ_SERVER
    }

}
