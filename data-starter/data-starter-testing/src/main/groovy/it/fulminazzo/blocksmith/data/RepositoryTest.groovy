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

    def 'test that save correctly updates #data'() {
        when:
        def saved = repository.save(data).get()

        then:
        saved == data

        when:
        def actual = repository.findById(data.id).get()

        then:
        actual.isPresent()
        actual.get() == data

        where:
        data << [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]
    }

    def 'test that save correctly saves #data'() {
        expect:
        !exists(data.id)

        when:
        def saved = repository.save(data).get()

        then:
        saved == data

        and:
        exists(data.id)

        where:
        data << [Users.NEW1, Users.NEW2]
    }

    def 'test that delete correctly deletes #data'() {
        expect:
        exists(data.id)

        when:
        repository.delete(data.id).get()

        then:
        !exists(data.id)

        where:
        data << [Users.SAVED1, Users.SAVED2]
    }

    def 'test that delete does not throw on not existing data'() {
        when:
        repository.delete(3L).get()

        then:
        noExceptionThrown()
    }

    def 'test that findAll returns all loaded data'() {
        when:
        def result = repository.findAll().get()

        then:
        result.sort() == [Users.SAVED1, Users.SAVED2].sort()
    }

    def 'test that findAllById correctly returns all data'() {
        given:
        def expected = [Users.SAVED1, Users.SAVED2]

        when:
        def actual = repository.findAllById([Users.SAVED1.id, Users.SAVED2.id, 3L]).get()

        then:
        actual == expected
    }

    def 'test that findAllById does not throw on null data'() {
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

    def 'test that saveAll correctly updates all data'() {
        given:
        def data = [
                new User(Users.SAVED1.id, Users.SAVED1.username + '_', Users.SAVED1.age + 1),
                new User(Users.SAVED2.id, Users.SAVED2.username + '_', Users.SAVED2.age + 1)
        ]

        when:
        def saved = repository.saveAll(data).get()

        then:
        saved == data

        when:
        def actual = repository.findAllById(data.collect { it.id }).get()

        then:
        actual == data
    }

    def 'test that saveAll correctly saves all data'() {
        given:
        def data = [Users.NEW1, Users.NEW2]

        expect:
        data.every { !exists(it.id) }

        when:
        def saved = repository.saveAll(data).get()

        then:
        saved.sort() == data.sort()

        and:
        data.every { exists(it.id) }
    }

    def 'test that saveAll does not throw on null data'() {
        given:
        def data = [Users.NEW1, Users.NEW2]

        expect:
        data.every { !exists(it.id) }

        when:
        def saved = repository.saveAll([null, Users.NEW1, null, Users.NEW2, null]).get()

        then:
        saved.sort() == data.sort()

        and:
        data.every { exists(it.id) }
    }

    def 'test that saveAll of empty returns empty'() {
        given:
        def data = []

        when:
        def saved = repository.saveAll(data).get()

        then:
        saved == data
    }

    def 'test that deleteAll correctly deletes all data'() {
        given:
        def data = [Users.SAVED1, Users.SAVED2]

        expect:
        data.every { exists(it.id) }

        when:
        repository.deleteAll([Users.SAVED1.id, Users.SAVED2.id, 3L])

        then:
        data.every { !exists(it.id) }
    }

    def 'test that deleteAll does not throw on null data'() {
        given:
        def data = [Users.SAVED1, Users.SAVED2]

        expect:
        data.every { exists(it.id) }

        when:
        repository.deleteAll([null, Users.SAVED1.id, null, Users.SAVED2.id, null])

        then:
        data.every { !exists(it.id) }
    }

    def 'test that deleteAll of empty does not throw'() {
        when:
        repository.deleteAll([])

        then:
        noExceptionThrown()
    }

    def 'test that count correctly returns number of data'() {
        when:
        def actual = repository.count().get()

        then:
        actual == 2L
    }

    abstract @NotNull R initializeRepository()

    /**
     * Checks if a data with the given id exists in the repository.
     *
     * @param id the id
     * @return <code>true</code> if it does
     */
    abstract boolean exists(final @NotNull Long id)

    /**
     * Adds the given data to the repository.
     *
     * @param data the data
     */
    abstract void insert(final @NotNull User data);

}
