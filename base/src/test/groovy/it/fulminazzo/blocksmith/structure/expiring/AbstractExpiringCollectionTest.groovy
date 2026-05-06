package it.fulminazzo.blocksmith.structure.expiring

import spock.lang.Specification

import java.time.Duration

class AbstractExpiringCollectionTest extends Specification {
    private static final Duration ttl = Duration.ofMillis(200)

    private static final String value = 'Hello, world!'

    private Collection<ExpiringEntry<String>> internal
    private AbstractExpiringCollection<String> collection

    void setup() {
        collection = new MockExpiringCollection<>()
        internal = collection.delegate
    }

    def 'test that add with TTL works'() {
        when:
        def result = collection.add(value, ttl)

        then:
        result

        and:
        internal.find { it.value == value }
        collection.contains(value)
    }

    def 'test that addAll with TTL collections correct TTLs'() {
        given:
        def second = [value]

        when:
        collection.addAll(second, ttl)
        final now = System.currentTimeMillis()

        then:
        internal.find { it.value == value }

        and:
        def actualTtl = internal.find { it.value == value }.expireTime - now
        actualTtl >= ttl.toMillis() - 10
        actualTtl <= ttl.toMillis() + 10
    }

    def 'test that addAll adds of expiring collection works'() {
        given:
        final secondValue = 'Goodbye, mars!'

        and:
        def second = new MockExpiringCollection()
        second.add('expired', 5)
        sleep(5)
        second.add(value, ttl)
        second.add(secondValue)

        when:
        collection.addAll((Collection<? extends String>) second)
        final now = System.currentTimeMillis()

        then:
        def firstVal = internal.find { it.value == value }
        def actualTtl = firstVal.expireTime - now
        actualTtl >= ttl.toMillis() - 10
        actualTtl <= ttl.toMillis() + 10

        and:
        internal.find { it.value == secondValue }

        and:
        internal.size() == 2
    }

    def 'test that addAll adds of general collection works'() {
        given:
        def second = [value]

        when:
        collection.addAll(second)

        then:
        internal.find { it.value == value }
    }

    def 'test that retainAll works'() {
        given:
        internal.add(new ExpiringEntry<String>(value, ExpiringEntry.NEVER_EXPIRE))

        when:
        def result = collection.retainAll(other)

        then:
        result == expected

        and:
        internal.find { it.value == value } != null == !expected

        where:
        other              || expected
        [value]            || false
        ['Goodbye, mars!'] || true
    }

    def 'test that removeAll works'() {
        given:
        internal.add(new ExpiringEntry<String>(value, ExpiringEntry.NEVER_EXPIRE))

        when:
        def result = collection.removeAll(other)

        then:
        result == expected

        and:
        internal.find { it.value == value } != null == !expected

        where:
        other              || expected
        [value]            || true
        ['Goodbye, mars!'] || false
    }

    def 'test that toString correctly prints expired entries'() {
        given:
        internal.add(new ExpiringEntry<String>('Hello', ttl.toMillis()))
        internal.add(new ExpiringEntry<String>('Goodbye', 1L))
        internal.add(new ExpiringEntry<String>('Ciao', ExpiringEntry.NEVER_EXPIRE))

        and:
        sleepTtl()

        when:
        def string = collection.toString()

        then:
        string == '[Goodbye (*), Hello, Ciao (!)]'
    }

    private static sleepTtl() {
        sleep(ttl.toMillis() / 2 as long)
    }

}
