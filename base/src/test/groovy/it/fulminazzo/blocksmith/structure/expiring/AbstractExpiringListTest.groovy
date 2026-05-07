package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration

class AbstractExpiringListTest extends Specification {
    private static final long ttl = 120_000L

    private static final String VALUE = 'Hello, world!'

    private static final ExpiringEntry<?> FIRST = new ExpiringEntry<>('First value', ttl)
    private static final ExpiringEntry<?> SECOND = new ExpiringEntry<>('Second value', Long.MAX_VALUE)
    private static final List<ExpiringEntry<?>> EXPECTED_ENTRIES = [FIRST, SECOND, FIRST]

    private AbstractExpiringList<String> list
    private List<ExpiringEntry<String>> internal

    void setup() {
        list = new MockExpiringList<>()
        internal = Reflect.on(list).get('delegate').get()
    }

    def 'test that getTtl works'() {
        given:
        list.add(VALUE)

        expect:
        list.getTtl(VALUE) != null
        list.getTtl(FIRST.value) == null
    }

    def 'test that getTtl does not throw if element is removed mid-execution'() {
        given:
        def list = Spy(MockExpiringList)
        list.indexOf(_) >> 1
        list.getExpiring(_) >> null

        expect:
        list.getTtl(VALUE) == null
    }

    def 'test that add correctly adds new entry'() {
        when:
        list.add(VALUE, ttl)
        def now = now()

        then:
        def entry = find(VALUE)
        entry != null

        and:
        def actualTtl = entry.expireTime - now
        actualTtl <= ttl
        actualTtl >= ttl * 0.9
    }

    def 'test that add with index correctly adds shifted entry'() {
        given:
        internal.addAll([FIRST, SECOND])

        when:
        if (expire == null) list.add(1, VALUE)
        else list.add(1, VALUE, expire)

        then:
        internal[0] == FIRST
        internal[1].value == VALUE
        internal[2] == SECOND

        where:
        expire << [null, Duration.ofSeconds(1)]
    }

    def 'test that addAll adds every element with the same TTL of the collection'() {
        given:
        internal.addAll([FIRST, FIRST])

        and:
        def other = Mock(ExpiringCollection)
        other.iterator() >> [
                SECOND.value,
                VALUE,
                'INVALID'
        ].iterator()
        other.getTtl(_) >> { a ->
            def val = a[0]
            if (val == SECOND.value) return Duration.ofMillis(ttl)
            else if (val == VALUE) return Duration.ofMillis(ExpiringEntry.NEVER_EXPIRE)
            else return null
        }

        when:
        list.addAll(1, (Collection<String>) other)

        then:
        internal[0] == FIRST
        internal[1].value == SECOND.value
        internal[2].value == VALUE
        internal[3] == FIRST

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element of expiring collection'() {
        given:
        internal.addAll([FIRST, FIRST])

        and:
        def other = Mock(ExpiringCollection)
        other.iterator() >> [
                SECOND.value,
                VALUE,
                'INVALID'
        ].iterator()
        other.getTtl(_) >> { a ->
            def val = a[0]
            if (val == SECOND.value) return Duration.ofMillis(ttl)
            else if (val == VALUE) return Duration.ofMillis(ExpiringEntry.NEVER_EXPIRE)
            else return null
        }

        when:
        list.addAll(1, (Collection<String>) other)
        def now = now()

        then:
        internal[0] == FIRST

        and:
        def first = internal[1]
        first != null
        first.value == SECOND.value
        def actualTtl = first.expireTime - now
        actualTtl <= ttl
        actualTtl >= ttl * 0.9

        and:
        def second = internal[2]
        second != null
        second.value == VALUE
        second.neverExpires()

        and:
        internal[3] == FIRST

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element with the same TTL'() {
        given:
        internal.addAll([FIRST, FIRST])

        and:
        def other = [SECOND.value, VALUE]

        when:
        list.addAll(1, other, Duration.ofMillis(ttl))
        def now = now()

        then:
        internal[0] == FIRST

        and:
        def first = internal[1]
        first != null
        first.value == SECOND.value
        def actualTtl1 = first.expireTime - now
        actualTtl1 >= ttl * 0.9
        actualTtl1 <= ttl

        and:
        def second = internal[2]
        second != null
        second.value == VALUE
        def actualTtl2 = second.expireTime - now
        actualTtl2 <= ttl
        actualTtl2 >= ttl * 0.9

        and:
        internal[3] == FIRST

        and:
        internal.size() == 4
    }

    def 'test that addAll adds every element with no expiration time'() {
        given:
        internal.addAll([FIRST, FIRST])

        and:
        def other = [SECOND.value, VALUE]

        when:
        list.addAll(1, other)

        then:
        internal[0] == FIRST

        and:
        def first = internal[1]
        first != null
        first.value == SECOND.value
        first.neverExpires()

        and:
        def second = internal[2]
        second != null
        second.value == VALUE
        second.neverExpires()

        and:
        internal[3] == FIRST

        and:
        internal.size() == 4
    }

    def 'test that set correctly overwrites value in list'() {
        given:
        internal.addAll([FIRST, FIRST])

        when:
        list.set(1, SECOND.value, Duration.ofMillis(ttl))
        def now = now()

        then:
        def actual = internal[1]
        actual != null
        actual.value == SECOND.value
        def actualTtl = actual.expireTime - now
        actualTtl >= ttl - 20
        actualTtl <= ttl + 20
    }

    def 'test that set of never expiring works'() {
        given:
        internal.addAll([FIRST, FIRST])

        when:
        list.set(1, SECOND.value)

        then:
        def actual = internal[1]
        actual != null
        actual.value == SECOND.value
        actual.neverExpires()
    }

    def 'test that indexOf works'() {
        given:
        internal.addAll([
                FIRST,
                SECOND,
                FIRST
        ])

        expect:
        list.indexOf(FIRST.value) == 0
        list.indexOf(SECOND.value) == 1
        list.indexOf('INVALID') == -1
    }

    def 'test that lastIndexOf works'() {
        given:
        internal.addAll([
                FIRST,
                SECOND,
                FIRST
        ])

        expect:
        list.lastIndexOf(FIRST.value) == 2
        list.lastIndexOf(SECOND.value) == 1
        list.lastIndexOf('INVALID') == -1
    }

    def 'test that iterator works'() {
        given:
        internal.addAll(EXPECTED_ENTRIES)

        when:
        def actual = []
        for (def i : list) actual.add(i)

        then:
        actual == EXPECTED_ENTRIES.collect { it.value }
    }

    def 'test that toArray works'() {
        given:
        internal.addAll(EXPECTED_ENTRIES)

        when:
        def actual = list.toArray()

        then:
        [*actual] == EXPECTED_ENTRIES.collect { it.value }
    }

    def 'test that toArray with smaller array creates new array'() {
        given:
        def previous = new String[]{'1'}

        and:
        internal.addAll(EXPECTED_ENTRIES)

        when:
        def actual = list.toArray(previous)

        then:
        [*actual] == EXPECTED_ENTRIES.collect { it.value }
        [*previous] == ['1']
    }

    def 'test that toArray with bigger array overwrites and creates sentinel'() {
        given:
        def previous = new String[size]

        and:
        internal.addAll(EXPECTED_ENTRIES)

        and:
        def expected = EXPECTED_ENTRIES.collect { it.value }
        if (size != EXPECTED_ENTRIES.size()) expected.add(null)

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
        internal.addAll(EXPECTED_ENTRIES)

        when:
        list.clear()

        then:
        internal.empty
    }

    def 'test that expiringEntries returns delegate'() {
        expect:
        list.expiringEntries() == internal
    }

    private ExpiringEntry<String> find(final String value) {
        return internal.find { it.value == value }
    }

    private static long now() {
        System.currentTimeMillis()
    }

}
