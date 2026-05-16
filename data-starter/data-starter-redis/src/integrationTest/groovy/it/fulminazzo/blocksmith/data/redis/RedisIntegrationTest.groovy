package it.fulminazzo.blocksmith.data.redis

import org.testcontainers.containers.GenericContainer

interface RedisIntegrationTest {
    int REDIS_PORT = 6379

    GenericContainer REDIS_SERVER = new GenericContainer('redis:7-alpine')
            .withExposedPorts(REDIS_PORT)
            .withReuse(true)

    default String getServerHost() {
        return container.host
    }

    default int getServerPort() {
        return container.getMappedPort(REDIS_PORT)
    }

    default GenericContainer getContainer() {
        if (!REDIS_SERVER.created) REDIS_SERVER.start()
        return REDIS_SERVER
    }

}
