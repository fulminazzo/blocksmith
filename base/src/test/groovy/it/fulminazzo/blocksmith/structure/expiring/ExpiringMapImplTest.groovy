package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

abstract class ExpiringMapImplTest extends Specification {

    private ExpiringMap<String, String> map
    private Map<String, AbstractExpiringMap.ExpiringEntry<String>> internal

    void setup() {
        map = createMap()
        internal = Reflect.on(map).get('delegate').get()
    }

    protected abstract ExpiringMap<String, String> createMap()

    def 'test that size does not count expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map.size() == 0
    }

    def 'test that size counts only non-expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        expect:
        map.size() == 1
    }

    def 'test that isEmpty returns true when all entries are expired'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map.isEmpty()
    }

    def 'test that isEmpty returns false when non-expired entries are present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        !map.isEmpty()
    }

    def 'test that containsKey returns false for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        !map.containsKey('Hello')
    }

    def 'test that containsKey returns true for non-expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        map.containsKey('Hello')
    }

    def 'test that get returns null for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map['Hello'] == null
    }

    def 'test that get returns value for non-expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)

        expect:
        map['Hello'] == 'world'
    }

    def 'test that get also clears other expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        map['Goodbye']

        then:
        internal['Hello'] == null
    }

    def 'test that remove returns null for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def actual = map.remove('Hello')

        then:
        actual == null
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

    def 'test that remove also clears other expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        map.remove('Goodbye')

        then:
        internal['Hello'] == null
    }

    def 'test that keySet does not include expired keys'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map.keySet().isEmpty()
    }

    def 'test that keySet includes only non-expired keys'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        def keys = map.keySet()

        then:
        !keys.contains('Hello')
        keys.contains('Goodbye')
    }

    def 'test that values does not include expired values'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map.values().isEmpty()
    }

    def 'test that values includes only non-expired values'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        def values = map.values()

        then:
        !values.contains('world')
        values.contains('mars')
    }

    def 'test that entrySet does not include expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        expect:
        map.entrySet().isEmpty()
    }

    def 'test that entrySet includes only non-expired entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        def entries = map.entrySet()

        then:
        entries.size() == 1
        entries.find { it.key == 'Goodbye' }?.value == 'mars'
    }

    def 'test that #method clears expired entries as a side effect'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 10000L)

        and:
        sleep(20L)

        when:
        map."$method"(*arguments)

        then:
        internal['Hello'] == null

        where:
        method         | arguments
        'size'         | []
        'isEmpty'      | []
        'containsKey'  | ['Goodbye']
        'get'          | ['Goodbye']
        'remove'       | ['Goodbye']
        'keySet'       | []
        'values'       | []
        'entrySet'     | []
    }

    def 'test that put treats expired key as absent'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def previous = map.put('Hello', 'moon', 10000L)

        then:
        previous == null

        and:
        internal['Hello'].value == 'moon'
    }

    def 'test that putIfAbsent treats expired key as absent'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def actual = map.putIfAbsent('Hello', 'moon', 10000L)

        then:
        actual == 'moon'
        internal['Hello'].value == 'moon'
    }

    def 'test that replace returns null for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def actual = map.replace('Hello', 'moon')

        then:
        actual == null
        internal['Hello'] == null
    }

    def 'test that computeIfAbsent treats expired key as absent'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def actual = map.computeIfAbsent('Hello', k -> 'moon', 10000L)

        then:
        actual == 'moon'
        internal['Hello'].value == 'moon'
    }

    def 'test that computeIfPresent returns null for expired key'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        and:
        sleep(20L)

        when:
        def actual = map.computeIfPresent('Hello', (k, v) -> v + 'moon')

        then:
        actual == null
        internal['Hello'] == null
    }

}