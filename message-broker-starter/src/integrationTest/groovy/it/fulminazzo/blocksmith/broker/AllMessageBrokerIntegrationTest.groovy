package it.fulminazzo.blocksmith.broker

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import it.fulminazzo.blocksmith.broker.config.MessageBrokerFactories
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageChannelSettings
import it.fulminazzo.blocksmith.broker.kafka.config.KafkaMessageBrokerConfig
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageChannelSettings
import it.fulminazzo.blocksmith.broker.memory.config.MemoryMessageBrokerConfig
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageChannelSettings
import it.fulminazzo.blocksmith.broker.plugin.config.PluginMessageBrokerConfig
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageChannelSettings
import it.fulminazzo.blocksmith.broker.rabbitmq.config.RabbitMQMessageBrokerConfig
import it.fulminazzo.blocksmith.broker.redis.RedisMessageChannelSettings
import it.fulminazzo.blocksmith.broker.redis.config.RedisMessageBrokerConfig
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageChannelSettings
import it.fulminazzo.blocksmith.broker.tcp.config.TcpMessageBrokerConfig
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaHelper
import spock.lang.Shared
import spock.lang.Specification

@Slf4j
class AllMessageBrokerIntegrationTest extends Specification {
    private static final int TCP_PORT = 16066
    private static final int REDIS_PORT = 6379
    private static final String CHANNEL_NAME = 'test:channel'

    private static final RabbitMQContainer RABBIT_MQ_SERVER = new RabbitMQContainer('rabbitmq:4.3.0-management-alpine')
            .withReuse(true)
    private static final GenericContainer REDIS_SERVER = new GenericContainer('redis:7-alpine')
            .withExposedPorts(REDIS_PORT)
            .withReuse(true)
    private static final ConfluentKafkaContainer KAFKA_SERVER = new ConfluentKafkaContainer('confluentinc/cp-kafka:7.4.0')
            .withReuse(true)

    @Shared
    private MessageBrokerConfig memoryMessageBrokerConfig

    @Shared
    private MessageBrokerConfig tcpMessageBrokerConfig

    @Shared
    private MessageBrokerConfig rabbitMQMessageBrokerConfig

    @Shared
    private MessageBrokerConfig redisMessageBrokerConfig

    @Shared
    private MessageBrokerConfig kafkaMessageBrokerConfig

    @Shared
    private MessageBrokerConfig pluginMessageBrokerConfig

    void setupSpec() {
        RABBIT_MQ_SERVER.start()
        REDIS_SERVER.start()
        KAFKA_SERVER.start()

        memoryMessageBrokerConfig = new MemoryMessageBrokerConfig()
        tcpMessageBrokerConfig = new TcpMessageBrokerConfig().setPort(TCP_PORT)
        rabbitMQMessageBrokerConfig = new RabbitMQMessageBrokerConfig()
                .setHost(RABBIT_MQ_SERVER.host)
                .setPort(RABBIT_MQ_SERVER.amqpPort)
        redisMessageBrokerConfig = new RedisMessageBrokerConfig()
                .setHost(REDIS_SERVER.host)
                .setPort(REDIS_SERVER.getMappedPort(REDIS_PORT))
        kafkaMessageBrokerConfig = new KafkaMessageBrokerConfig()
                .setBootstrapServers([
                        new KafkaMessageBrokerConfig.BootstrapServerConfig(
                                KAFKA_SERVER.host,
                                KAFKA_SERVER.getMappedPort(KafkaHelper.KAFKA_PORT)
                        )
                ].toSet())
        pluginMessageBrokerConfig = new PluginMessageBrokerConfig()
                .setOwner(this)
    }

    def 'test #messageBrokerConfig broker life cycle'() {
        given:
        final AllMessageChannelSettings messageChannelSettings = new AllMessageChannelSettings(
                new MemoryMessageChannelSettings(),
                new TcpMessageChannelSettings(),
                new RabbitMQMessageChannelSettings()
                        .withQueueName('queue')
                        .withQueueSettings { it.durable() },
                new RedisMessageChannelSettings(),
                new KafkaMessageChannelSettings()
                        .idempotenceWithDefaults()
                        .withMessagesKey('broker-test-message')
                        .withGroupId('integration-tests-group')
                        .withAutoCommit(1_000),
                new PluginMessageChannelSettings()
        ).withChannelName(CHANNEL_NAME).broadcast()

        when:
        def messageBroker = MessageBrokerFactories.build(messageBrokerConfig)

        then:
        noExceptionThrown()

        when:
        def channel = messageBroker.newChannel(
                messageChannelSettings.getMessageChannelSettings(messageBroker)
        )

        then:
        channel != null

        when:
        channel.send('Hello, world!').join()

        then:
        noExceptionThrown()

        when:
        channel.close()

        then:
        noExceptionThrown()

        when:
        messageBroker.close()

        then:
        noExceptionThrown()

        where:
        messageBrokerConfig << [
                memoryMessageBrokerConfig,
                tcpMessageBrokerConfig,
                rabbitMQMessageBrokerConfig,
                redisMessageBrokerConfig,
                kafkaMessageBrokerConfig,
                pluginMessageBrokerConfig
        ]
    }

}
