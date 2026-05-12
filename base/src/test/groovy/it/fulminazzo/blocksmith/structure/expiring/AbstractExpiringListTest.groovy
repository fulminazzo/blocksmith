package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration

class AbstractExpiringListTest extends Specification {
    private static final long ttl = 120_000L

    private static final String value = 'Hello, world!'

    private static final ExpiringEntry<?> first = new ExpiringEntry<>('First value', ttl)
    private static final ExpiringEntry<?> second = new ExpiringEntry<>('Second value', Long.MAX_VALUE)
    private static final List<ExpiringEntry<?>> expectedEntries = [first, second, first]

    private AbstractExpiringList<String> list
    private List<ExpiringEntry<String>> internal

    void setup() {
        list = new MockExpiringList<>()
        internal = Reflect.on(list).get('delegate').get()
    }

    def 'test that getTtl works'() {
        given:
        list.add(value)

        expect:
        list.getTtl(value) != null
        list.getTtl(first.value) == null
    }

    def 'test that getTtl does not throw if element is removed mid-execution'() {
        given:
        def list = Spy(MockExpiringList)
        list.indexOf(_) >> 1
        list.getExpiring(_) >> null

        expect:
        list.getTtl(value) == null
    }

    def 'test that add correctly adds new entry'() {
        when:
        list.add(value, ttl)
        def now = now()

        then:
        def entry = find(value)
        entry != null

        and:
        def actualTtl = entry.expireTime - now
        actualTtl <= ttl
        actualTtl >= ttl * 0.9
    }

    def 'test that add with index correctly adds shifted entry'() {
        given:
        internal.addAll([first, second])

        when:
        if (expire == null) list.add(1, value)
        else list.add(1, value, expire)

        then:
        internal[0] == first
        internal[1].value == value
        internal[2] == second

        where:
        expire << [null, Duration.ofSeconds(1)]
    }

    def 'test that addAll adds every element with the same TTL of the collection'() {
        given:
        internal.addAll([first, first])

        and:
        def other = Mock(ExpiringCollection)
        other.iterator() >> [
                second.value,
                value,
                'INVALID'
        ].iterator()
        other.getTtl(_) >> { a ->
            def val = a[0]
            if (val == second.value) return Duration.ofMillis(ttl)
            else if (val == value) return Duration.ofMillis(ExpiringEntry.NEVER_EXPIRE)
            else return null
        }

        when:
        list.addAll(1, (Collection<String>) other)

        then:
        internal[0] == first
        internal[1].value == second.value
        internal[2].value == value
        internal[3] == first

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element of expiring collection'() {
        given:
        internal.addAll([first, first])

        and:
        def other = Mock(ExpiringCollection)
        other.iterator() >> [
                second.value,
                value,
                'INVALID'
        ].iterator()
        other.getTtl(_) >> { a ->
            def val = a[0]
            if (val == second.value) return Duration.ofMillis(ttl)
            else if (val == value) return Duration.ofMillis(ExpiringEntry.NEVER_EXPIRE)
            else return null
        }

        when:
        list.addAll(1, (Collection<String>) other)
        def now = now()

        then:
        internal[0] == first

        and:
        def first = internal[1]
        first != null
        first.value == second.value
        def actualTtl = first.expireTime - now
        actualTtl <= ttl
        actualTtl >= ttl * 0.9

        and:
        def second = internal[2]
        second != null
        second.value == value
        second.neverExpires()

        and:
        internal[3] == AbstractExpiringListTest.first

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element with the same TTL'() {
        given:
        internal.addAll([first, first])

        and:
        def other = [second.value, value]

        when:
        list.addAll(1, other, Duration.ofMillis(ttl))
        def now = now()

        then:
        internal[0] == first

        and:
        def first = internal[1]
        first != null
        first.value == second.value
        def actualTtl1 = first.expireTime - now
        actualTtl1 >= ttl * 0.9
        actualTtl1 <= ttl

        and:
        def second = internal[2]
        second != null
        second.value == value
        def actualTtl2 = second.expireTime - now
        actualTtl2 <= ttl
        actualTtl2 >= ttl * 0.9

        and:
        internal[3] == AbstractExpiringListTest.first

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element with no expiration time'() {
        given:
        internal.addAll([first, first])

        and:
        def other = [second.value, value]

        when:
        list.addAll(1, other)

        then:
        internal[0] == first

        and:
        def first = internal[1]
        first != null
        first.value == second.value
        first.neverExpires()

        and:
        def second = internal[2]
        second != null
        second.value == value
        second.neverExpires()

        and:
        internal[3] == AbstractExpiringListTest.first

        and:
        internal.size() == 4
    }

    def 'test that set correctly overwrites value in list'() {
        given:
        internal.addAll([first, first])

        when:
        list.set(1, second.value, Duration.ofMillis(ttl))
        def now = now()

        then:
        def actual = internal[1]
        actual != null
        actual.value == second.value
        def actualTtl = actual.expireTime - now
        actualTtl >= ttl - 20
        actualTtl <= ttl + 20
    }

    def 'test that set of never expiring works'() {
        given:
        internal.addAll([first, first])

        when:
        list.set(1, second.value)

        then:
        def actual = internal[1]
        actual != null
        actual.value == second.value
        actual.neverExpires()
    }

    def 'test that indexOf works'() {
        given:
        internal.addAll([
                first,
                second,
                first
        ])

        expect:
        list.indexOf(first.value) == 0
        list.indexOf(second.value) == 1
        list.indexOf('INVALID') == -1
    }

    def 'test that lastIndexOf works'() {
        given:
        internal.addAll([
                first,
                second,
                first
        ])

        expect:
        list.lastIndexOf(first.value) == 2
        list.lastIndexOf(second.value) == 1
        list.lastIndexOf('INVALID') == -1
    }

    def 'test that iterator works'() {
        given:
        internal.addAll(expectedEntries)

        when:
        def actual = []
        for (def i : list) actual.add(i)

        then:
        actual == expectedEntries*.value
    }

    def 'test that toArray works'() {
        given:
        internal.addAll(expectedEntries)

        when:
        def actual = list.toArray()

        then:
        [*actual] == expectedEntries*.value
    }

    def 'test that toArray with smaller array creates new array'() {
        given:
        def previous = new String[]{'1'}

        and:
        internal.addAll(expectedEntries)

        when:
        def actual = list.toArray(previous)

        then:
        [*actual] == expectedEntries*.value
        [*previous] == ['1']
    }

    def 'test that toArray with bigger array overwrites and creates sentinel'() {
        given:
        def previous = new String[size]

        and:
        internal.addAll(expectedEntries)

        and:
        def expected = expectedEntries*.value
        if (size != expectedEntries.size()) expected.add(null)

        when:
        def actual = list.toArray(previous)

        then:
        [*actual] == expected
        [*previous] == expected

        where:
        size << [3, 4]
    }

    def 'test that clear clears delegate'() {
        given:
        internal.addAll(expectedEntries)

        when:
        list.clear()

        then:
        internal.empty
    }

    def 'test that expiringEntries returns delegate'() {
        expect:
        list.expiringEntries() == internal
    }

    protected ExpiringEntry<String> find(final String value) {
        return internal.find { it.value == value }
    }

    protected static long now() {
        return System.currentTimeMillis()
    }

}
