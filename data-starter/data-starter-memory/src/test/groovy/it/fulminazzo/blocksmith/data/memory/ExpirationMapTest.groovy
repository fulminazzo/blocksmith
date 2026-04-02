package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.validation.ViolationException
import spock.lang.Specification

class ExpirationMapTest extends Specification {
    private static final int EXPIRATION_TIME = 100

    private ExpirationMap<String, String> map

    void setup() {
        map = new ExpirationMap<>(EXPIRATION_TIME)
    }

    def 'test that initialize throws for #arguments'() {
        when:
        new ExpirationMap<>(*arguments)

        then:
        thrown(ViolationException)

        where:
        arguments << [
                [-1],
                [[:], -1]
        ]
    }

    def 'test that size returns size of non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.putAll(['hi': 'friend'])

        expect:
        map.size() == 1
    }

    def 'test that isEmpty returns true only for non-expired values'() {
        expect:
        map.isEmpty()

        when:
        map.putAll(['hello': 'world'])

        then:
        !map.isEmpty()

        when:
        Thread.sleep(EXPIRATION_TIME)

        then:
        map.isEmpty()
    }

    def 'test that containsKey returns true only for non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        when:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.put('hello', 'world')

        then:
        map.containsKey('hello')

        and:
        !map.containsKey('goodbye')
    }

    def 'test that containsValue returns true only for non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        when:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.put('hello', 'world')

        then:
        map.containsValue('world')

        and:
        !map.containsValue('mars')
    }

    def 'test that get returns only non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        when:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.put('hello', 'world')

        then:
        map.get('hello') == 'world'

        and:
        map.get('goodbye') == null

        and:
        map.get('hi') == null
    }

    def 'test that put returns only previous non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        when:
        def actual = map.put('hello', 'earth')

        then:
        actual == null

        when:
        actual = map.put('hello', 'world')

        then:
        actual == 'earth'
    }

    def 'test that remove returns only previous non-expired values'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        when:
        def actual = map.remove('hello')

        then:
        actual == null

        when:
        map.put('hello', 'earth')

        and:
        actual = map.remove('hello')

        then:
        actual == 'earth'
    }

    def 'test that keySet returns only non-expired keys'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.putAll(['hi': 'friend'])

        expect:
        map.keySet() == ['hi'].toSet()
    }

    def 'test that values returns only non-expired keys'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.putAll(['hi': 'friend'])

        expect:
        map.values() == ['friend']
    }

    def 'test that entrySet returns only non-expired keys'() {
        given:
        map.putAll(['hello': 'world', 'goodbye': 'mars'])

        and:
        Thread.sleep(EXPIRATION_TIME)

        and:
        map.putAll(['hi': 'friend'])

        expect:
        map.entrySet() == [Map.entry('hi', 'friend')].toSet()
    }

    def 'test that values do not expire on 0 expiration time'() {
        given:
        map.setExpiry(0)

        and:
        map.put('hello', 'world')

        and:
        Thread.sleep(EXPIRATION_TIME)

        when:
        def actual = map.get('hello')

        then:
        actual == 'world'
    }

}
