package it.fulminazzo.blocksmith.broker.redis

import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import redis.embedded.RedisServer
import spock.lang.Specification

class RedisMessageBrokerTest extends Specification {
    private static final int serverPort = 26380

    private static RedisServer server

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()
    }

    void cleanupSpec() {
        server?.stop()
    }

    def 'test that server is online'() {
        expect:
        server.active
    }

    def 'test broker life cycle'() {
        given:
        def MessageBroker = RedisMessageBroker.builder()
                .uri(b ->
                        b.withHost('localhost')
                                .withPort(serverPort)
                )
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(MapperFormat.JSON.newMapper())
                .build()

        when:
        def directChannel = MessageBroker.newChannel(
                new RedisMessageChannelSettings()
                        .withChannelName('main')
                        .direct('sub')
        )

        then:
        directChannel != null

        when:
        def broadcastChannel = MessageBroker.newChannel(
                new RedisMessageChannelSettings()
                        .withChannelName('main')
                        .broadcast()
        )

        then:
        broadcastChannel != null

        when:
        MessageBroker.close()

        then:
        noExceptionThrown()
    }

}
