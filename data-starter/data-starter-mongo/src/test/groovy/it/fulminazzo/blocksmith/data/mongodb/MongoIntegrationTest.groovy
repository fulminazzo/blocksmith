package it.fulminazzo.blocksmith.data.mongodb

import org.testcontainers.containers.MongoDBContainer

interface MongoIntegrationTest {
    int MONGO_PORT = 27017

    MongoDBContainer MONGO_SERVER = new MongoDBContainer('mongo:7.0')

    default String getConnectionString() {
        return container.connectionString
    }

    default String getServerHost() {
        return container.host
    }

    default int getServerPort() {
        return MONGO_SERVER.getMappedPort(MONGO_PORT)
    }

    default MongoDBContainer getContainer() {
        if (!MONGO_SERVER.created) MONGO_SERVER.start()
        return MONGO_SERVER
    }

}
