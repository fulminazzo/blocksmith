package it.fulminazzo.blocksmith.data

import java.time.Duration

abstract class CacheRepositoryIntegrationTest<R extends CacheRepository<User, Long>> extends RepositoryIntegrationTest<R> {
    private static final long TTL = 2_000

    def 'test that save stores #entity with TTL and after TTL the entity is not found'() {
        when:
        def saved = repository.ttl(Duration.ofMillis(TTL)).save(entity).get()

        then:
        saved == entity

        when:
        def first = repository.findById(entity.id).get()

        then:
        first.present
        first.get() == entity

        when:
        sleep(TTL)

        and:
        def second = repository.findById(entity.id).get()

        then:
        second.empty

        where:
        entity << [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]
    }

    def 'test that saveAll correctly stores all entities with TTL and after TTL all the entities are not found'() {
        given:
        def entities = [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]

        when:
        def saved = repository.ttl(Duration.ofMillis(TTL)).saveAll(entities).get()

        then:
        saved == entities

        when:
        def first = repository.findAllById(entities*.id).get()

        then:
        first == entities

        when:
        sleep(TTL)

        and:
        def second = repository.findAllById(entities*.id).get()

        then:
        second.empty
    }

}
