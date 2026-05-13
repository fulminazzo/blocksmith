package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringMapDelayedTest extends Specification {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor()

    private ScheduledExpiringMap<String, String> map
    private Map<String, ExpiringEntry<String>> internal

    void cleanupSpec() {
        SCHEDULER.close()
    }

    void setup() {
        map = new ScheduledExpiringMap<>(SCHEDULER, Duration.ofDays(1L))
        internal = Reflect.on(map).get('delegate').get()
    }

    def 'test that initialization of invalid duration throws'() {
        when:
        new ScheduledExpiringMap<>(SCHEDULER, Duration.ofSeconds(0))

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that getExpiring returns null if expired but not yet removed'() {
        given:
        internal['Hello'] = new ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def entry = map.getExpiring('Hello')

        then:
        entry == null

        and:
        internal['Hello'] == null
    }

    def 'test that remove returns null if expired but not yet removed'() {
        given:
        internal['Hello'] = new ExpiringEntry<>('world', 1L)

        and:
        sleep(5L)

        when:
        def entry = map.remove('Hello')

        then:
        entry == null

        and:
        internal['Hello'] == null
    }

    def 'test that keySet does not return expired but not yet removed keys'() {
        given:
        internal['Hello'] = new ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new ExpiringEntry<>('mars', 1000L)

        and:
        sleep(5L)

        when:
        def keys = map.keySet()

        then:
        keys == ['Goodbye'].toSet()
    }

    def 'test that values does not return expired but not yet removed values'() {
        given:
        internal['Hello'] = new ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new ExpiringEntry<>('mars', 1000L)

        and:
        sleep(5L)

        when:
        def values = map.values()

        then:
        values == ['mars']
    }

}
