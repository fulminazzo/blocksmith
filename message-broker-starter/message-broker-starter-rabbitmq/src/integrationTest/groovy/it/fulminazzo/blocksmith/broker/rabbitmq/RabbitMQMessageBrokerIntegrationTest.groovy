package it.fulminazzo.blocksmith.broker.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

import java.util.concurrent.Executors

@Slf4j
class RabbitMQMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<RabbitMQMessageChannelSettings> {

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<RabbitMQMessageChannelSettings>> newMessageBrokerBuilder() {
        return RabbitMQMessageBroker.builder()
                .host(RabbitMQIntegrationTest.serverHost)
                .port(RabbitMQIntegrationTest.serverPort)
                .username(ConnectionFactory.DEFAULT_USER)
                .password(ConnectionFactory.DEFAULT_PASS)
                .configure {
                    it.connectionTimeout = 60_000
                }
                .executor(Executors.newCachedThreadPool())
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new RabbitMQChannelIntegrationTestHelper(channelName, log)
    }

    @Override
    protected RabbitMQMessageChannelSettings getSettings() {
        return new RabbitMQMessageChannelSettings()
                .withQueueName(RabbitMQChannelIntegrationTestHelper.QUEUE_NAME)
                .durable()
                .withQueueSettings { RabbitMQMessageChannelSettings.QueueSettings queueSettings ->
                    queueSettings
                            .durable()
                            .addArgument('testing', true)
                }
    }

}
