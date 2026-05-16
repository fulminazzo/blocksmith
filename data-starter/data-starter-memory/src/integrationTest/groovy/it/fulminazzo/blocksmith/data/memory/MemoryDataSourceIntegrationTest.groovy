package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import spock.lang.Specification

import java.time.Duration

class MemoryDataSourceIntegrationTest extends Specification {

    def 'test synchronous datasource life cycle'() {
        given:
        def dataSource = MemoryDataSource.create()

        when:
        def repository = dataSource.newRepository(
                User,
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofSeconds(1))
                        .withExpirationStrategy(MemoryRepositorySettings.ExpiryStrategy.LAZY)
        )

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    def 'test asynchronous datasource life cycle'() {
        given:
        def dataSource = MemoryDataSource.createAsync()

        when:
        def repository = dataSource.newRepository(
                User,
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofSeconds(1))
                        .withExpirationStrategy(MemoryRepositorySettings.ExpiryStrategy.SCHEDULED)
        )

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        dataSource.executor.shutdown
    }

    def 'test expiry save-find cycle with #expirationStrategy'() {
        given:
        final ttl = 2_000L
        final entity = new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1)

        and:
        final repository = MemoryDataSource.create().newRepository(
                User,
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofMillis(ttl))
                        .withExpirationStrategy(expirationStrategy)
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

        where:
        expirationStrategy << MemoryRepositorySettings.ExpiryStrategy.values()
    }

}
