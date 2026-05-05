package it.fulminazzo.blocksmith.structure.expiring

import spock.lang.Specification

import java.time.Duration

class DelegateExpiringSetTest extends Specification {
    private static final long ttl = 200L

    private static final String value = 'Hello, world!'
    private static final Object PRESENT = DelegateExpiringSet.PRESENT

    private DelegateExpiringSet<String> set
    private ExpiringMap<String, Object> internal

    void setup() {
        internal = new MockExpiringMap<>()
        set = new DelegateExpiringSet(internal)
    }

    def 'test that add with TTL adds and updates'() {
        when:
        def result = set.add(value, ttl)

        then:
        result

        and:
        internal.containsKey(value)
        set.contains(value)

        when:
        result = set.add(value, ttl)

        then:
        !result

        and:
        internal.containsKey(value)
        set.contains(value)
    }

    def 'test that add without TTL adds and updates'() {
        when:
        def result = set.add(value)

        then:
        result

        and:
        internal.containsKey(value)

        when:
        result = set.add(value)

        then:
        !result

        and:
        internal.containsKey(value)
    }

    def 'test that remove works'() {
        given:
        internal[value] = PRESENT

        when:
        def result = set.remove(value)

        then:
        result

        and:
        !internal.containsKey(value)

        when:
        result = set.remove(value)

        then:
        !result

        and:
        !internal.containsKey(value)
    }

    def 'test that containsAll works'() {
        given:
        internal[value] = PRESENT

        when:
        def result = set.containsAll(collection)

        then:
        result == expected

        where:
        collection                || expected
        [value]                   || true
        [value, 'Goodbye, mars!'] || false
        ['Goodbye, mars!']        || false
    }

    def 'test that addAll with ttl sets correct TTLs'() {
        given:
        def second = [value]

        when:
        set.addAll(second, Duration.ofMillis(ttl))

        then:
        internal.containsKey(value)

        and:
        def actualTtl = internal.getTtl(value)
        actualTtl.toMillis() >= ttl - 10
        actualTtl.toMillis() <= ttl + 10
    }

    def 'test that addAll adds of expiring set works'() {
        given:
        final secondValue = 'Goodbye, mars!'

        and:
        def second = new DelegateExpiringSet(ExpiringMap.passive())
        second.add(value, ttl)
        second.add(secondValue)

        when:
        set.addAll((Collection<? extends String>) second)

        then:
        internal.containsKey(value)

        and:
        def actualTtl = internal.getTtl(value)
        actualTtl.toMillis() >= ttl - 10
        actualTtl.toMillis() <= ttl + 10

        and:
        internal.containsKey(secondValue)
    }

    def 'test that addAll adds of general collection works'() {
        given:
        def second = [value]

        when:
        set.addAll(second)

        then:
        internal.containsKey(value)
    }

    def 'test that retainAll works'() {
        given:
        internal[value] = PRESENT

        when:
        def result = set.retainAll(collection)

        then:
        result == expected

        and:
        internal.containsKey(value) == !expected

        where:
        collection         || expected
        [value]            || false
        ['Goodbye, mars!'] || true
    }

    def 'test that removeAll works'() {
        given:
        internal[value] = PRESENT

        when:
        def result = set.removeAll(collection)

        then:
        result == expected

        and:
        internal.containsKey(value) == !expected

        where:
        collection         || expected
        [value]            || true
        ['Goodbye, mars!'] || false
    }

    def 'test that #method(#arguments) calls on delegate #delegateMethod'() {
        given:
        def internal = Mock(AbstractExpiringMap)
        def set = new DelegateExpiringSet(internal)

        when:
        set."$method"(*arguments)

        then:
        1 * internal."$delegateMethod"() >> { a ->
            if (delegateMethod == 'keySet') return [].toSet()
            else if (delegateMethod == 'size') return 0
        }

        where:
        method     | arguments       || delegateMethod
        'clear'    | []              || 'clear'
        'size'     | []              || 'size'
        'isEmpty'  | []              || 'isEmpty'
        'iterator' | []              || 'keySet'
        'toArray'  | []              || 'keySet'
        'toArray'  | [new String[2]] || 'keySet'
        'hashCode' | []              || 'keySet'
    }

    def 'test that set is equal to self'() {
        given:
        internal['Hello'] = PRESENT

        expect:
        Object.getMethod('equals', Object).invoke(set, set)

        and:
        set.hashCode() == set.hashCode()
    }

    def 'test that equals with #object returns #expected'() {
        given:
        internal.put('Hello', PRESENT, 1000L)
        internal.put('Goodbye', PRESENT, 1L)

        and:
        sleepTtl()

        when:
        def actual = Objects.equals(set, object?.toSet())

        then:
        actual == expected

        where:
        object                                  || expected
        null                                    || false
        'Hello=world'                           || false
        []                                      || false
        ['Hello']                               || true
        ['Goodbye']                             || false
        new DelegateExpiringSet(new MockExpiringMap() {
            {
                put('Hello', PRESENT, 1000L)
            }
        })                                      || true
        new DelegateExpiringSet(new MockExpiringMap() {
            {
                put('Goodbye', PRESENT, 1000L)
            }
        })                                      || false
    }

    def 'test that toString correctly prints expired entries'() {
        given:
        internal.put('Hello', PRESENT, ttl)
        internal.put('Goodbye', PRESENT, 1L)
        internal.put('Ciao', PRESENT, AbstractExpiringMap.NEVER_EXPIRE)

        and:
        sleepTtl()

        when:
        def string = set.toString()

        then:
        string == '[Goodbye (*), Hello, Ciao (!)]'
    }

    def 'test that expiringEntries returns correct entries'() {
        given:
        def now = System.currentTimeMillis()
        internal.put('Hello', PRESENT, ttl)
        internal.put('Goodbye', PRESENT, 1L)
        internal.put('Ciao', PRESENT, AbstractExpiringMap.NEVER_EXPIRE)

        when:
        def data = set.expiringEntries()

        then:
        def first = data.find {it.value == 'Hello'}
        first != null
        first.expireTime - now <= ttl + 10
        first.expireTime - now >= ttl - 10

        and:
        def second = data.find {it.value == 'Goodbye'}
        second != null
        second.expireTime - now <= 1 + 10
        second.expireTime - now >= 1 - 10

        and:
        def third = data.find {it.value == 'Ciao'}
        third != null
        third.expireTime == AbstractExpiringMap.NEVER_EXPIRE
    }

    private static sleepTtl() {
        sleep(ttl / 2 as long)
    }

}
