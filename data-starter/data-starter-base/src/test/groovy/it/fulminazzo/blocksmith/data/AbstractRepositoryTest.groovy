package it.fulminazzo.blocksmith.data

import it.fulminazzo.blocksmith.Cat
import it.fulminazzo.blocksmith.MockRepository
import spock.lang.Specification

import java.util.concurrent.ExecutionException

class AbstractRepositoryTest extends Specification {

    private AbstractRepository<Cat, String, ?> repository

    void setup() {
        repository = new MockRepository()
    }

    def 'test that save does not throw on valid entity'() {
        given:
        def entity = new Cat('Sissi', 15, true)

        when:
        def saved = repository.save(entity).get()

        then:
        saved == entity
    }

    def 'test that save throws exception on invalid entity'() {
        given:
        def entity = new Cat('Sissi', -1, true)

        when:
        repository.save(entity).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def ex = e.cause
        ex != null
        ex.message == 'Cat age should at least be 1'
    }

    def 'test that delete returns null'() {
        given:
        def entity = new Cat('Sissi', 18, true)

        and:
        repository.save(entity).get()

        when:
        def actual = repository.delete(entity.name).get()

        then:
        actual == null
    }

    def 'test that findAllById ignores null ids'() {
        given:
        def expected = [
                new Cat('Sissi', 18, true),
                new Cat('Calimero', 4, false),
                new Cat('Junior', 7, true)
        ]

        and:
        repository.saveAll(expected).get()

        when:
        def actual = repository.findAllById([
                null, *expected.collect { it.name }, null
        ]).get()

        then:
        actual == expected
    }

    def 'test that saveAll does not throw on valid entities'() {
        given:
        def expected = [
                new Cat('Sissi', 18, true),
                new Cat('Calimero', 4, false),
                new Cat('Junior', 7, true)
        ]

        when:
        def saved = repository.saveAll(expected).get()

        then:
        saved == expected
    }

    def 'test that saveAll throws exception on invalid entities'() {
        given:
        def entities = [
                new Cat('Sissi', 18, true),
                new Cat(null, 4, false),
                new Cat('Junior', 7, true)
        ]

        when:
        repository.saveAll(entities).get()

        then:
        def e = thrown(ExecutionException)

        and:
        def ex = e.cause
        ex != null
        ex.message == 'Cat name should not be empty'
    }

    def 'test that deleteAll ignores null ids'() {
        given:
        def expected = [
                new Cat('Sissi', 18, true),
                new Cat('Calimero', 4, false),
                new Cat('Junior', 7, true)
        ]

        and:
        repository.saveAll(expected).get()

        when:
        def actual = repository.deleteAll([
                null, *expected.collect { it.name }, null
        ]).get()

        then:
        actual == null

        when:
        def data = repository.findAllById(
                expected.collect { it.name }
        ).get()

        then:
        data.isEmpty()
    }

}
