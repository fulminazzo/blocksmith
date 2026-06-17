package it.fulminazzo.blocksmith.broker.redis

import org.testcontainers.containers.GenericContainer

interface RedisIntegrationTest {
    static final int REDIS_PORT = 6379

    static final GenericContainer REDIS_SERVER = new GenericContainer('redis:7-alpine')
            .withExposedPorts(REDIS_PORT)
            .withReuse(true)

    static String getServerHost() {
        return container.host
    }

    static int getServerPort() {
        return container.getMappedPort(REDIS_PORT)
    }

    static GenericContainer getContainer() {
        if (!REDIS_SERVER.created) REDIS_SERVER.start()
        return REDIS_SERVER
    }

}
