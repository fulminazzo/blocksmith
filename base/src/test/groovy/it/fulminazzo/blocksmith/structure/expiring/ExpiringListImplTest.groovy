package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

abstract class ExpiringListImplTest extends Specification {
    private static final long ttl = 400L
    private static final long expiringTtl = ttl / 4 as long

    private final ExpiringEntry<String> first = new ExpiringEntry<>('Hello', ttl)
    private final ExpiringEntry<String> second = new ExpiringEntry<>('friend', expiringTtl)
    private final ExpiringEntry<String> third = new ExpiringEntry<>('world', ttl)

    private ExpiringList<String> list
    private List<ExpiringEntry<String>> internal

    void setup() {
        list = createList()
        internal = Reflect.on(list).get('delegate').get()
    }

    protected abstract ExpiringList<String> createList()

    def 'test that indexed add does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        and:
        list.add(1, 'dear', ttl)

        then:
        internal[0].value == first.value
        internal[1].value == 'dear'
        internal[2].value == third.value
        internal.size() == 3
    }

    def 'test that indexed set does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        and:
        def previous = list.set(1, 'dear', ttl)

        then:
        internal[0].value == first.value
        internal[1].value == 'dear'
        internal.size() == 2

        and:
        previous == third.value
    }

    def 'test that get does not return expired entries'() {
        given:
        internal.add(first)
        internal.add(second)

        when:
        sleepTtl()

        then:
        list.get(0) == first.value

        when:
        list.get(1)

        then:
        thrown(IndexOutOfBoundsException)
    }

    def 'test that remove index does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)

        when:
        sleepTtl()

        then:
        list.remove(0) == first.value

        when:
        list.remove(0)

        then:
        thrown(IndexOutOfBoundsException)
    }

    def 'test that subList does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        and:
        def sublist = list.subList(0, 2)

        then:
        sublist[0] == first.value
        sublist[1] == third.value
        sublist.size() == 2
    }

    def 'test that #method does not consider expired entries'() {
        given:
        internal.add(second)

        when:
        sleepTtl()

        then:
        list."$method"() == expected

        where:
        method    || expected
        'size'    || 0
        'isEmpty' || true
    }

    def 'test that #method does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        then:
        list."$method"(first.value)
        !list."$method"(second.value)
        list."$method"(third.value)

        where:
        method << ['contains', 'remove']
    }

    def 'test that containsAll does not consider expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        then:
        list.containsAll([first].collect { it.value })
        !list.containsAll([second].collect { it.value })
        list.containsAll([third].collect { it.value })
        !list.containsAll([first, second].collect { it.value })
        !list.containsAll([second, third].collect { it.value })
        list.containsAll([first, third].collect { it.value })
        !list.containsAll([first, second, third].collect { it.value })
    }

    private static void sleepTtl() {
        sleep(ttl / 2 as long)
    }

}