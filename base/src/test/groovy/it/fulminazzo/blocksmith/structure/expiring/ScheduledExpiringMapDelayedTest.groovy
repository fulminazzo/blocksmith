package it.fulminazzo.blocksmith.structure.expiring

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class ScheduledExpiringMapDelayedTest extends Specification {
    private static ScheduledExecutorService scheduler

    private ScheduledExpiringMap<String, String> map
    private Map<String, AbstractExpiringMap.ExpiringEntry<String>> internal

    void setupSpec() {
        scheduler = Executors.newSingleThreadScheduledExecutor()
    }

    void cleanupSpec() {
        scheduler.close()
    }

    void setup() {
        map = new ScheduledExpiringMap<>(scheduler, Duration.ofDays(1L))
        internal = Reflect.on(map).get('delegate').get()
    }

    def 'test that getExpiring returns null if expired but not yet removed'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        when:
        def entry = map.getExpiring('Hello')

        then:
        entry == null

        and:
        internal['Hello'] == null
    }

    def 'test that remove returns null if expired but not yet removed'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)

        when:
        def entry = map.remove('Hello')

        then:
        entry == null

        and:
        internal['Hello'] == null
    }

    def 'test that keySet does not return expired but not yet removed keys'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 1000L)

        when:
        def keys = map.keySet()

        then:
        keys == ['Goodbye'].toSet()
    }

    def 'test that values does not return expired but not yet removed values'() {
        given:
        internal['Hello'] = new AbstractExpiringMap.ExpiringEntry<>('world', 1L)
        internal['Goodbye'] = new AbstractExpiringMap.ExpiringEntry<>('mars', 1000L)

        when:
        def values = map.values()

        then:
        values == ['mars']
    }

}
