package it.fulminazzo.blocksmith.broker

import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageBroker
import it.fulminazzo.blocksmith.broker.kafka.KafkaMessageChannelSettings
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageBroker
import it.fulminazzo.blocksmith.broker.memory.MemoryMessageChannelSettings
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageBroker
import it.fulminazzo.blocksmith.broker.plugin.PluginMessageChannelSettings
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageBroker
import it.fulminazzo.blocksmith.broker.rabbitmq.RabbitMQMessageChannelSettings
import it.fulminazzo.blocksmith.broker.redis.RedisMessageBroker
import it.fulminazzo.blocksmith.broker.redis.RedisMessageChannelSettings
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageBroker
import it.fulminazzo.blocksmith.broker.tcp.TcpMessageChannelSettings
import spock.lang.Specification

class AllMessageChannelSettingsTest extends Specification {
    private static final MemoryMessageChannelSettings MEMORY = new MemoryMessageChannelSettings()
    private static final TcpMessageChannelSettings TCP = new TcpMessageChannelSettings()
    private static final RabbitMQMessageChannelSettings RABBITMQ = new RabbitMQMessageChannelSettings()
    private static final RedisMessageChannelSettings REDIS = new RedisMessageChannelSettings()
    private static final KafkaMessageChannelSettings KAFKA = new KafkaMessageChannelSettings()
    private static final PluginMessageChannelSettings PLUGIN = new PluginMessageChannelSettings()

    private static final AllMessageChannelSettings SETTINGS = AllMessageChannelSettings.builder()
            .memory(MEMORY)
            .tcp(TCP)
            .rabbitMQ(RABBITMQ)
            .redis(REDIS)
            .kafka(KAFKA)
            .plugin(PLUGIN)
            .build()

    def 'test that getMessageChannelSettings returns #expected with #messageBroker'() {
        when:
        def actual = SETTINGS.getMessageChannelSettings(messageBroker)

        then:
        actual == expected

        where:
        messageBroker               || expected
        Mock(MemoryMessageBroker)   || MEMORY
        Mock(TcpMessageBroker)      || TCP
        Mock(RabbitMQMessageBroker) || RABBITMQ
        Mock(RedisMessageBroker)    || REDIS
        Mock(KafkaMessageBroker)    || KAFKA
        Mock(PluginMessageBroker)   || PLUGIN
    }

    def 'test that getMessageChannelSettings throws for unrecognized data source'() {
        when:
        SETTINGS.getMessageChannelSettings(Mock(MessageBroker))

        then:
        thrown(IllegalArgumentException)
    }

}
