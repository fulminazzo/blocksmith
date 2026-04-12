package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class PassiveExpiringMapTest extends Specification {

    private ExpiringMap<String, String> map
    private Map<String, AbstractExpiringMap.ExpiringEntry<String>> internal

    void setup() {
        map = new PassiveExpiringMap<>()
        internal = Reflect.on(map).get('delegate').get()
    }

    def 'test that size counts expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        map.size() == 1
    }

    def 'test that size counts also expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        expect:
        map.size() == 2
    }

    def 'test that isEmpty returns false even if all entries are expired'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        !map.isEmpty()
    }

    def 'test that isEmpty returns false when non-expired entries are present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        !map.isEmpty()
    }

    def 'test that containsKey returns true for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        map.containsKey('Hello')
    }

    def 'test that containsKey returns true for non-expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        map.containsKey('Hello')
    }

    def 'test that get returns value for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        map['Hello'] == 'world'
    }

    def 'test that get returns value for non-expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        map['Hello'] == 'world'
    }

    def 'test that get does not clear other expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        map['Goodbye']

        then:
        map['Hello'] == 'world'
    }

    def 'test that remove does not return null for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def actual = map.remove('Hello')

        then:
        actual == 'world'
        internal['Hello'] == null
    }

    def 'test that remove returns value for non-expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        when:
        def actual = map.remove('Hello')

        then:
        actual == 'world'
        internal['Hello'] == null
    }

    def 'test that remove does not clear other expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        map.remove('Goodbye')

        then:
        internal['Hello'] != null
    }

    def 'test that keySet includes expired keys'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        !map.keySet().isEmpty()
    }

    def 'test that keySet includes also expired keys'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        def keys = map.keySet()

        then:
        keys.contains('Hello')
        keys.contains('Goodbye')
    }

    def 'test that values includes expired values'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        !map.values().isEmpty()
    }

    def 'test that values includes also expired values'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        def values = map.values()

        then:
        values.contains('world')
        values.contains('mars')
    }

    def 'test that entrySet includes expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        expect:
        !map.entrySet().isEmpty()
    }

    def 'test that entrySet includes also expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        def entries = map.entrySet()

        then:
        entries.size() == 2
        entries.find { it.key == 'Goodbye' }?.value == 'mars'
        entries.find { it.key == 'Hello' }?.value == 'world'
    }

    def 'test that #method does not clear expired entries as a side effect'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(5L)

        when:
        map."$method"(*arguments)

        then:
        internal['Hello'] != null

        where:
        method        | arguments
        'size'        | []
        'isEmpty'     | []
        'containsKey' | ['Goodbye']
        'get'         | ['Goodbye']
        'remove'      | ['Goodbye']
        'keySet'      | []
        'values'      | []
        'entrySet'    | []
    }

    def 'test that put treats expired key as present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def previous = map.put('Hello', 'moon', 10000L)

        then:
        previous == 'world'

        and:
        internal['Hello'].value == 'moon'
    }

    def 'test that putIfAbsent treats expired key as present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def actual = map.putIfAbsent('Hello', 'moon', 10000L)

        then:
        actual == 'world'
        internal['Hello'].value == 'world'
    }

    def 'test that replace returns value for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def actual = map.replace('Hello', 'moon')

        then:
        actual == 'world'
        internal['Hello'] != null
    }

    def 'test that computeIfAbsent treats expired key as present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def actual = map.computeIfAbsent('Hello', k -> 'moon', 10000L)

        then:
        actual == 'world'
        internal['Hello'].value == 'moon'
    }

    def 'test that computeIfPresent returns value for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def actual = map.computeIfPresent('Hello', (k, v) -> v + 'moon')

        then:
        actual == 'world'
        internal['Hello'] == null
    }

}
