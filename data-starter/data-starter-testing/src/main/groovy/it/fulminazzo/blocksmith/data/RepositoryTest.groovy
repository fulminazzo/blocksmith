package it.fulminazzo.blocksmith.data

import org.jetbrains.annotations.NotNull
import spock.lang.Specification

abstract class RepositoryTest<R extends Repository<User, Long>> extends Specification {
    protected static final User FIRST = new User(1, 'fulminazzo', 23)
    protected static final User SECOND = new User(2, 'c4my', 20)
    protected static final User UPDATE1 = new User(3, 'tiz_', 55)
    protected static final User UPDATE2 = new User(4, 'alex', 18)

    protected R repository

    void setupRepository() {
        repository = initializeRepository()
        insert(FIRST)
        insert(SECOND)
    }

    def 'test that findById returns #expected'() {
        when:
        def actual = repository.findById(expected.id).get()

        then:
        actual.isPresent()
        actual.get() == expected

        where:
        expected << [FIRST, SECOND]
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
        FIRST   || true
        SECOND  || true
        UPDATE1 || false
        UPDATE2 || false
    }

    def 'test that findAll returns all loaded data'() {
        when:
        def result = repository.findAll().get()

        then:
        result.sort() == [FIRST, SECOND].sort()
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
                new User(FIRST.id, FIRST.username + '_', FIRST.age + 1),
                new User(SECOND.id, SECOND.username + '_', SECOND.age + 1)
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
        data << [UPDATE1, UPDATE2]
    }

    def 'test that delete correctly deletes #data'() {
        expect:
        exists(data.id)

        when:
        repository.delete(data.id).get()

        then:
        !exists(data.id)

        where:
        data << [FIRST, SECOND]
    }

    def 'test that findAllById correctly returns all data'() {
        given:
        def expected = [FIRST, SECOND]

        when:
        def actual = repository.findAllById(expected.collect { it.id }).get()

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
                new User(FIRST.id, FIRST.username + '_', FIRST.age + 1),
                new User(SECOND.id, SECOND.username + '_', SECOND.age + 1)
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
        def data = [UPDATE1, UPDATE2]

        expect:
        data.every { !exists(it.id) }

        when:
        def saved = repository.saveAll(data).get()

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
        def data = [FIRST, SECOND]

        expect:
        data.every { exists(it.id) }

        when:
        repository.deleteAll(data.collect { it.id })

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
