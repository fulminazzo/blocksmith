package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Function

class AbstractExpiringMapTest extends Specification {

    private static final Function<? super String, ? extends String> function = { k -> 'world' }
    private static final BiFunction<? super String, ? super String, ? extends String> bifunction = { (k, v) -> 'moon' }

    private AbstractExpiringMap<String, String> map
    private Map<String, AbstractExpiringMap.ExpiringEntry<String>> internal

    void setup() {
        map = new MockExpiringMap<>()
        internal = Reflect.on(map).get('delegate').get()
    }

    def 'test that put updates if not present'() {
        when:
        def actual = map.put('Hello', 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'moon'

        and:
        actual == null
    }

    def 'test that put updates if present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def actual = map.put('Hello', 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'moon'

        and:
        actual == 'world'
    }

    def 'test that containsValue of #value returns #expected'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def actual = map.containsValue(value)

        then:
        actual == expected

        where:
        value   || expected
        'world' || true
        'mars'  || false
    }

    def 'test that putIfAbsent updates if not present'() {
        when:
        def actual = map.putIfAbsent('Hello', 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'moon'
        entry.value == actual
    }

    def 'test that putIfAbsent does not update if present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def actual = map.putIfAbsent('Hello', 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'world'
        entry.value == actual
    }

    def 'test that replace correctly replaces with time to live'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('mars', 10L)
        def previousTime = previous.expireTime
        internal['Hello'] = previous

        when:
        def actual = map.replace('Hello', 'mars', 'world', 20L)

        then:
        actual

        and:
        def entry = internal['Hello']
        entry != null
        entry.value == previous.value
        entry.value == 'world'
        entry.expireTime == previous.expireTime
        entry.expireTime - previousTime >= 10L
    }

    def 'test that replace of #key and #oldValue does not replace'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>(null, 10L)

        when:
        def actual = map.replace(key, oldValue, 'world', 10L)

        then:
        !actual

        where:
        key       | oldValue
        'Hello'   | 'mars'
        'Goodbye' | 'mars'
        'Salut'   | null
    }

    def 'test that replace without TTL correctly replaces'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('mars', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.replace('Hello', 'mars', 'world')

        then:
        actual

        and:
        def entry = internal['Hello']
        entry != null
        entry.value == previous.value
        entry.value == 'world'
        entry.expireTime == previous.expireTime
    }

    def 'test that replace without TTL of #key and #oldValue does not replace'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>(null, 10L)

        when:
        def actual = map.replace(key, oldValue, 'world')

        then:
        !actual

        where:
        key       | oldValue
        'Hello'   | 'mars'
        'Goodbye' | 'mars'
        'Salut'   | null
    }

    def 'test that replace without TTL and old value correctly replaces'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('mars', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.replace('Hello', 'world')

        then:
        actual

        and:
        def entry = internal['Hello']
        entry != null
        entry.value == previous.value
        entry.value == 'world'
        entry.expireTime == previous.expireTime
    }

    def 'test that replace without TTL of #key does not replace'() {
        given:
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>(null, 10L)

        when:
        def actual = map.replace(key, 'world')

        then:
        !actual

        where:
        key << ['Goodbye', 'Salut']
    }

    def 'test that computeIfAbsent updates if not present'() {
        when:
        def actual = map.computeIfAbsent('Hello', k -> 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'moon'
        entry.value == actual
    }

    def 'test that computeIfAbsent does not update if present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def actual = map.computeIfAbsent('Hello', k -> 'moon', 10L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'world'
        entry.value == actual
    }

    def 'test that computeIfPresent updates if present'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def actual = map.computeIfPresent('Hello', (k, v) -> v + 'moon')

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'worldmoon'
        entry.value == actual
    }

    def 'test that computeIfPresent does not update if not present'() {
        when:
        def actual = map.computeIfPresent('Hello', (k, v) -> 'moon')

        then:
        def entry = internal['Hello']
        entry == null
        actual == null
    }

    def 'test that compute correctly adds new value'() {
        when:
        def actual = map.compute('Hello', (k, v) -> 'world', 10L)

        then:
        actual == null

        and:
        def entry = internal['Hello']
        entry != null
        entry.value == 'world'
    }

    def 'test that compute correctly updates existing value'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.compute('Hello', (k, v) -> v + 'moon', 20L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'worldmoon'
        entry.expireTime - previous.expireTime >= 10L

        and:
        actual == previous.value
    }

    def 'test that compute correctly removes existing value'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.compute('Hello', (k, v) -> null, 10L)

        then:
        def entry = internal['Hello']
        entry == null

        and:
        actual == previous.value
    }

    def 'test that merge correctly adds new value'() {
        when:
        def actual = map.merge('Hello', 'world', (k, v) -> 'world', 10L)

        then:
        actual == 'world'

        and:
        def entry = internal['Hello']
        entry != null
        entry.value == 'world'
    }

    def 'test that merge correctly updates existing value'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.merge('Hello', 'world', (k, v) -> v + 'moon', 20L)

        then:
        def entry = internal['Hello']
        entry != null
        entry.value == 'worldmoon'
        entry.expireTime - previous.expireTime >= 10L

        and:
        actual == 'worldmoon'
    }

    def 'test that merge correctly removes existing value'() {
        given:
        def previous = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Hello'] = previous

        when:
        def actual = map.merge('Hello', 'world', (k, v) -> null, 10L)

        then:
        def entry = internal['Hello']
        entry == null

        and:
        actual == null
    }

    def 'test that merge does not throw for not existing'() {
        when:
        def actual = map.merge('Hello', null, (k, v) -> null, 10L)

        then:
        def entry = internal['Hello']
        entry == null

        and:
        actual == null
    }

    def 'test that putAll adds every element with same TTL'() {
        given:
        def map = ['Hello': 'world', 'Goodbye': 'mars']

        and:
        def now = System.currentTimeMillis()

        when:
        this.map.putAll(map, 10L)

        then:
        def first = internal['Hello']
        first != null
        first.value == 'world'
        first.expireTime >= now

        and:
        def second = internal['Goodbye']
        second != null
        second.value == 'mars'
        second.expireTime >= now
    }

    def 'test that putAll without TTL adds every non-expired key'() {
        given:
        def map = Mock(ExpiringMap)
        map.getTtl(_) >> { a -> a[0] == 'Hello' ? Duration.ofMillis(10L) : null }
        map.forEach(_) >> { a ->
            BiConsumer consumer = a[0]
            consumer.accept('Hello', 'world')
            consumer.accept('Goodbye', 'mars')
        }

        when:
        this.map.putAll(map)

        then:
        def first = internal['Hello']
        first != null
        first.value == 'world'
        first.expireTime >= System.currentTimeMillis()

        and:
        internal['Goodbye'] == null
    }

    def 'test that entrySet returns all entries'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 2000L)

        when:
        def entries = map.entrySet()

        then:
        def first = entries.find { it.key == 'Hello' }
        first != null
        first.value == 'world'

        and:
        def second = entries.find { it.key == 'Goodbye' }
        second != null
        second.value == 'mars'
    }

    def 'test that clear delegates to internal'() {
        given:
        def internal = Mock(Map)
        def map = new MockExpiringMap()
        Reflect.on(map).set('delegate', internal)

        when:
        map.clear()

        then:
        1 * internal.clear()
    }

    def 'test that getTtl of existing returns correct value'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)

        when:
        def duration = map.getTtl('Hello')

        then:
        duration != null

        and:
        def millis = duration.toMillis()
        millis >= 9L
        millis <= 10L
    }

    def 'test that getTtl of non-existing returns null'() {
        when:
        def duration = map.getTtl('Hello')

        then:
        duration == null
    }

    def 'test that getTtl of non-existing throws NoSuchElementException'() {
        when:
        map.renew('Hello', 1L)

        then:
        thrown(NoSuchElementException)
    }

    def 'test that renew correctly applies new time-to-live'() {
        given:
        def entry = new AbstractExpiringMap.ExpiringEntry<>('world', 10L)
        internal['Hello'] = entry

        and:
        def now = System.currentTimeMillis()

        when:
        map.renew('Hello', 20L)

        then:
        def millis = entry.expireTime - now
        millis >= 19L
        millis <= 21L
    }

    def 'test that #method(#arguments) throws invalid TTL exception'() {
        when:
        map."$method"(*arguments)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'time-to-live must be more than 0'

        where:
        method            | arguments
        'put'             | ['Hello', 'world', -1L]
        'putIfAbsent'     | ['Hello', 'world', -1L]
        'replace'         | ['Hello', 'mars', 'world', -1L]
        'computeIfAbsent' | ['Hello', function, -1L]
        'compute'         | ['Hello', bifunction, -1L]
        'merge'           | ['Hello', 'world', bifunction, -1L]
        'putAll'          | [[:], -1]
        'renew'           | ['Hello', -1L]
    }

    def 'test that #method(#arguments) delegates to #expectedMethod(#expectedArguments)'() {
        given:
        def map = Mock(AbstractExpiringMap)
        def matchCount = 0

        when:
        map."$method"(*arguments)

        then:
        _ * map."$expectedMethod"(*_) >> { a ->
            if (a == expectedArguments) matchCount++
            else callRealMethod()
        }
        matchCount == 1

        where:
        method            | arguments                                               || expectedMethod    | expectedArguments
        'put'             | ['Hello', 'world', Duration.ofMillis(1337)]             || 'put'             | ['Hello', 'world', 1337L]
        'putIfAbsent'     | ['Hello', 'world', Duration.ofMillis(1337)]             || 'putIfAbsent'     | ['Hello', 'world', 1337L]
        'replace'         | ['Hello', 'world', 'moon', Duration.ofMillis(1337)]     || 'replace'         | ['Hello', 'world', 'moon', 1337L]
        'computeIfAbsent' | ['Hello', function, Duration.ofMillis(1337)]            || 'computeIfAbsent' | ['Hello', function, 1337L]
        'compute'         | ['Hello', bifunction, Duration.ofMillis(1337)]          || 'compute'         | ['Hello', bifunction, 1337L]
        'merge'           | ['Hello', 'world', bifunction, Duration.ofMillis(1337)] || 'merge'           | ['Hello', 'world', bifunction, 1337L]
        'putAll'          | [[:], Duration.ofMillis(1337)]                          || 'putAll'          | [[:], 1337]
        'renew'           | ['Hello', Duration.ofMillis(1337)]                      || 'renew'           | ['Hello', 1337L]
    }

    def 'test that invocation of #method throws UnsupportedOperationException'() {
        given:
        def map = Mock(AbstractExpiringMap)

        and:
        map."$method"(*_) >> {
            callRealMethod()
        }

        when:
        map."$method"(*arguments)

        then:
        def e = thrown(UnsupportedOperationException)
        e.message == "${map.getClass().simpleName} does not support put without TTL"

        where:
        method            | arguments
        'put'             | ['Hello', 'world']
        'putIfAbsent'     | ['Hello', 'world']
        'computeIfAbsent' | ['Hello', function]
        'compute'         | ['Hello', bifunction]
        'merge'           | ['Hello', 'world', bifunction]
        'putAll'          | [[:]]
    }

    /**
     * ExpiringEntryMapEntry
     */

    def 'test that entry getters and setter work'() {
        given:
        def entry = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        expect:
        entry.key == 'Hello'

        and:
        entry.value == 'world'

        when:
        entry.value = 'mars'

        then:
        entry.key == 'Hello'

        and:
        entry.value == 'mars'
    }

    def 'test that entry equals self'() {
        given:
        def first = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        and:
        def second = Map.entry('Hello', 'world')

        expect:
        first.equals(second)
    }

    def 'test that entry does not equal #other'() {
        given:
        def entry = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        expect:
        !entry.equals(other)

        where:
        other << [
                null,
                Map.entry('Goodbye', 'mars'),
                Map.entry('Hello', 'mars'),
                Map.entry('Goodbye', 'world')
        ]
    }

    def 'test that entry hashcode equals self'() {
        given:
        def first = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        and:
        def second = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        expect:
        first.hashCode() == second.hashCode()
    }

    def 'test that entry hashcode does not equal #other'() {
        given:
        def entry = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 10000L)
        )

        expect:
        entry.hashCode() != other.hashCode()

        where:
        other << [
                Map.entry('Goodbye', 'mars'),
                Map.entry('Hello', 'mars'),
                Map.entry('Goodbye', 'world')
        ]
    }

    def 'test that #method(#arguments) throws IllegalStateException if expired'() {
        given:
        def entry = new AbstractExpiringMap.ExpiringEntryMapEntry<>(
                'Hello',
                new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        )

        and:
        sleep(1L)

        when:
        entry."$method"(*arguments)

        then:
        thrown(IllegalStateException)

        where:
        method     | arguments
        'getKey'   | []
        'getValue' | []
        'setValue' | ['mars']
        'equals'   | ['mars']
        'hashCode' | []
    }

}
