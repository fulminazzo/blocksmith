package it.fulminazzo.blocksmith.data.redis

import it.fulminazzo.blocksmith.data.DataSourceIntegrationTest
import it.fulminazzo.blocksmith.data.RepositoryDataSource
import it.fulminazzo.blocksmith.data.RepositoryDataSourceBuilder
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.MapperFormat

import java.time.Duration

class RedisDataSourceIntegrationTest extends DataSourceIntegrationTest<RedisRepositorySettings> implements RedisIntegrationTest {

    def 'test expiry save-find cycle'() {
        given:
        final ttl = 2_000L
        final entity = new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1)

        and:
        def dataSource = newDataSourceBuilder().build()

        and:
        def repository = dataSource.newRepository(
                User,
                settings.withTtl(Duration.ofMillis(ttl))
        )

        when:
        def saved = repository.save(entity).get()

        then:
        saved == entity

        when:
        def first = repository.findById(entity.id).get()

        then:
        first.present
        first.get() == entity

        when:
        sleep(ttl)

        and:
        def second = repository.findById(entity.id).get()

        then:
        second.empty

        cleanup:
        dataSource?.close()
    }

    def 'test datasource life cycle with explicit EntityMapper'() {
        given:
        def builder = newDataSourceBuilder()

        when:
        def dataSource = builder.build()

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(
                User,
                settings.withEntityMapper(EntityMapper.create(User, 'username'))
        )

        then:
        repository != null

        when:
        def user = repository.findById('Alex').join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

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
    }

}
