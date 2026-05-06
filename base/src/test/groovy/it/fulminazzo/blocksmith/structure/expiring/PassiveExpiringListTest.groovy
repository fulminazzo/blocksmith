package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class PassiveExpiringListTest extends Specification {
    private static final long ttl = 400L
    private static final long expiringTtl = ttl / 4 as long

    private final ExpiringEntry<String> first = new ExpiringEntry<>('Hello', ttl)
    private final ExpiringEntry<String> second = new ExpiringEntry<>('friend', expiringTtl)
    private final ExpiringEntry<String> third = new ExpiringEntry<>('world', ttl)

    private ExpiringList<String> list
    private List<ExpiringEntry<String>> internal

    void setup() {
        list = new PassiveExpiringList<>()
        internal = Reflect.on(list).get('delegate').get()
    }

    def 'test that indexed add considers expired entries'() {
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
        internal[2].value == second.value
        internal[3].value == third.value
        internal.size() == 4
    }

    def 'test that indexed set considers expired entries'() {
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
        internal[2].value == third.value
        internal.size() == 3

        and:
        previous == second.value
    }

    def 'test that get also returns expired entries'() {
        given:
        internal.add(first)
        internal.add(second)

        when:
        sleepTtl()

        then:
        list.get(0) == first.value

        and:
        list.get(1) == second.value
    }

    def 'test that remove index considers expired entries'() {
        given:
        internal.add(first)
        internal.add(second)

        when:
        sleepTtl()

        then:
        list.remove(0) == first.value

        and:
        list.remove(0) == second.value
    }

    def 'test that subList considers expired entries'() {
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
        sublist[1] == second.value
        sublist.size() == 2
    }

    def 'test that #method considers expired entries'() {
        given:
        internal.add(second)

        when:
        sleepTtl()

        then:
        list."$method"() == expected

        where:
        method    || expected
        'size'    || 1
        'isEmpty' || false
    }

    def 'test that #method considers expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        then:
        list."$method"(first.value)
        list."$method"(second.value)
        list."$method"(third.value)

        where:
        method << ['contains', 'remove']
    }

    def 'test that containsAll considers expired entries'() {
        given:
        internal.add(first)
        internal.add(second)
        internal.add(third)

        when:
        sleepTtl()

        then:
        list.containsAll([first].collect { it.value })
        list.containsAll([second].collect { it.value })
        list.containsAll([third].collect { it.value })
        list.containsAll([first, second].collect { it.value })
        list.containsAll([second, third].collect { it.value })
        list.containsAll([first, third].collect { it.value })
        list.containsAll([first, second, third].collect { it.value })
    }

    def 'test that iterator returns expired entries'() {
        given:
        def entries = [first, second, third]
        internal.addAll(entries)

        and:
        sleepTtl()

        when:
        def actual = []
        for (def i : list) actual.add(i)

        then:
        actual == entries.collect { it.value }
    }

    private static void sleepTtl() {
        sleep(ttl / 2 as long)
    }

}