package it.fulminazzo.blocksmith.data.redis

import it.fulminazzo.blocksmith.data.DataSourceIntegrationTest
import it.fulminazzo.blocksmith.data.RepositoryDataSource
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

import java.time.Duration

class RedisDataSourceIntegrationTest extends DataSourceIntegrationTest<RedisRepositorySettings> implements RedisIntegrationTest {

    @Override
    protected RepositoryDataSourceBuilder<RepositoryDataSource<RedisRepositorySettings>> newDataSourceBuilder() {
        return RedisDataSource.builder()
                .uri(b -> b.withHost(serverHost).withPort(serverPort))
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(MapperFormat.JSON.newMapper())
    }

    @Override
    protected RedisRepositorySettings getSettings() {
        return new RedisRepositorySettings()
                .withDatabaseName('database')
                .withCollectionName('users')
                .withTtl(Duration.ofSeconds(1))
    }

}
