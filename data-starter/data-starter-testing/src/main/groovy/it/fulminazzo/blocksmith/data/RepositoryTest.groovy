package it.fulminazzo.blocksmith.data

import org.jetbrains.annotations.NotNull
import spock.lang.Specification

abstract class RepositoryTest<R extends Repository<User, Long>> extends Specification {
    protected R repository

    void setupRepository() {
        repository = initializeRepository()
        insert(Users.SAVED1)
        insert(Users.SAVED2)
    }
    
    void clearData() {
        remove(Users.SAVED1.id)
        remove(Users.SAVED2.id)
        remove(Users.NEW1.id)
        remove(Users.NEW2.id)
    }

    def 'test that findById returns #expected'() {
        when:
        def actual = repository.findById(expected.id).get()

        then:
        actual.isPresent()
        actual.get() == expected

        where:
        expected << [Users.SAVED1, Users.SAVED2]
    }

    def 'test that findById does not throw if not existing'() {
        when:
        def actual = repository.findById(3L).get()

        then:
        !actual.isPresent()
    }

    def 'test that existsById of #user returns #expected'() {
        when:
        def actual = repository.existsById(user.id).get()

        then:
        actual == expected

        where:
        user    || expected
        Users.SAVED1 || true
        Users.SAVED2 || true
        Users.NEW1 || false
        Users.NEW2 || false
    }

    def 'test that save correctly updates #entity'() {
        when:
        def saved = repository.save(entity).get()

        then:
        saved == entity

        when:
        def actual = repository.findById(entity.id).get()

        then:
        actual.isPresent()
        actual.get() == entity

        where:
        entity << [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]
    }

    def 'test that save correctly saves #entity'() {
        expect:
        !exists(entity.id)

        when:
        def saved = repository.save(entity).get()

        then:
        saved == entity

        and:
        exists(entity.id)

        where:
        entity << [Users.NEW1, Users.NEW2]
    }

    def 'test that delete correctly deletes #entity'() {
        expect:
        exists(entity.id)

        when:
        repository.delete(entity.id).get()

        then:
        !exists(entity.id)

        where:
        entity << [Users.SAVED1, Users.SAVED2]
    }

    def 'test that delete does not throw on not existing entity'() {
        when:
        repository.delete(3L).get()

        then:
        noExceptionThrown()
    }

    def 'test that findAll returns all loaded entity'() {
        when:
        def result = repository.findAll().get()

        then:
        result.sort() == [Users.SAVED1, Users.SAVED2].sort()
    }

    def 'test that findAllById correctly returns all entity'() {
        given:
        def expected = [Users.SAVED1, Users.SAVED2]

        when:
        def actual = repository.findAllById([Users.SAVED1.id, Users.SAVED2.id, 3L]).get()

        then:
        actual == expected
    }

    def 'test that findAllById does not throw on null entity'() {
        given:
        def expected = [Users.SAVED1, Users.SAVED2]

        when:
        def actual = repository.findAllById([null, Users.SAVED1.id, null, Users.SAVED2.id, null]).get()

        then:
        actual == expected
    }

    def 'test that findAllById of empty returns empty'() {
        given:
        def expected = []

        when:
        def actual = repository.findAllById([]).get()

        then:
        actual == expected
    }

    def 'test that saveAll correctly updates all entity'() {
        given:
        def entity = [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]

        when:
        def saved = repository.saveAll(entity).get()

        then:
        saved == entity

        when:
        def actual = repository.findAllById(entity.collect { it.id }).get()

        then:
        actual == entity
    }

    def 'test that saveAll correctly saves all entity'() {
        given:
        def entity = [Users.NEW1, Users.NEW2]

        expect:
        entity.every { !exists(it.id) }

        when:
        def saved = repository.saveAll(entity).get()

        then:
        saved.sort() == entity.sort()

        and:
        entity.every { exists(it.id) }
    }

    def 'test that saveAll does not throw on null entity'() {
        given:
        def entity = [Users.NEW1, Users.NEW2]

        expect:
        entity.every { !exists(it.id) }

        when:
        def saved = repository.saveAll([null, Users.NEW1, null, Users.NEW2, null]).get()

        then:
        saved.sort() == entity.sort()

        and:
        entity.every { exists(it.id) }
    }

    def 'test that saveAll of empty returns empty'() {
        given:
        def entity = []

        when:
        def saved = repository.saveAll(entity).get()

        then:
        saved == entity
    }

    def 'test that deleteAll correctly deletes all entity'() {
        given:
        def entity = [Users.SAVED1, Users.SAVED2]

        expect:
        entity.every { exists(it.id) }

        when:
        repository.deleteAll([Users.SAVED1.id, Users.SAVED2.id, 3L])

        then:
        entity.every { !exists(it.id) }
    }

    def 'test that deleteAll does not throw on null entity'() {
        given:
        def entity = [Users.SAVED1, Users.SAVED2]

        expect:
        entity.every { exists(it.id) }

        when:
        repository.deleteAll([null, Users.SAVED1.id, null, Users.SAVED2.id, null])

        then:
        entity.every { !exists(it.id) }
    }

    def 'test that deleteAll of empty does not throw'() {
        when:
        repository.deleteAll([])

        then:
        noExceptionThrown()
    }

    def 'test that count correctly returns number of entity'() {
        when:
        def actual = repository.count().get()

        then:
        actual == 2L
    }

    abstract @NotNull R initializeRepository()

    /**
     * Checks if a entity with the given id exists in the repository.
     *
     * @param id the entity id
     * @return <code>true</code> if it does
     */
    abstract boolean exists(final @NotNull Long id)

    /**
     * Adds the given entity to the repository.
     *
     * @param entity the entity
     */
    abstract void insert(final @NotNull User entity);

    /**
     * Removes the entity with the given id from the repository.
     *
     * @param entity the entity id
     */
    abstract void remove(final @NotNull Long id);

}
