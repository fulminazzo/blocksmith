package it.fulminazzo.blocksmith.broker.kafka

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.broker.MessageBroker
import it.fulminazzo.blocksmith.broker.MessageBrokerBuilder
import it.fulminazzo.blocksmith.broker.MessageBrokerIntegrationTest
import it.fulminazzo.blocksmith.broker.MessageChannelIntegrationTestHelper
import org.apache.kafka.clients.ClientDnsLookup
import org.apache.kafka.common.security.auth.SecurityProtocol

import java.util.concurrent.Executors

@Slf4j
class KafkaMessageBrokerIntegrationTest extends MessageBrokerIntegrationTest<KafkaMessageChannelSettings> {

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected MessageBrokerBuilder<MessageBroker<KafkaMessageChannelSettings>> newMessageBrokerBuilder() {
        return KafkaMessageBroker.builder()
                .bootstrapServer(
                        KafkaChannelIntegrationTestHelper.serverHost,
                        KafkaChannelIntegrationTestHelper.serverPort
                )
                .securityProtocol(SecurityProtocol.PLAINTEXT)
                .clientDnsLookup(ClientDnsLookup.USE_ALL_DNS_IPS)
                .reconnectBackoff(100L)
                .reconnectBackoffMax(50_000L)
                .requestTimeout(60_000L)
                .executor(Executors.newCachedThreadPool())
                .addProperty('test', 'property')
    }

    @Override
    protected MessageChannelIntegrationTestHelper newTestHelper(final String channelName) {
        return new KafkaChannelIntegrationTestHelper(channelName.replace(':', '.'), log)
    }

    @Override
    protected KafkaMessageChannelSettings getSettings() {
        return new KafkaMessageChannelSettings()
                .withAssignmentWaitTime(120_000L)
                .withPollInterval(75L)
                .idempotenceWithDefaults()
                .withMessagesKey('broker-test-message')
                .withGroupId('integration-tests-group')
                .withAutoCommit(1_000L)
                .withSessionAndHeartbeat(5_000L)
    }

}
