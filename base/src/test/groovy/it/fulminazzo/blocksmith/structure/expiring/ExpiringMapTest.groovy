package it.fulminazzo.blocksmith.structure.expiring

import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors

class ExpiringMapTest extends Specification {

    def 'test that #method with #arguments returns #type map'() {
        when:
        def map = ExpiringMap."$method"(*arguments)

        then:
        (map.getClass() == type)

        where:
        method      | arguments                                                            || type
        'passive'   | []                                                                   || PassiveExpiringMap
        'lazy'      | []                                                                   || LazyExpiringMap
        'scheduled' | [Executors.newSingleThreadScheduledExecutor(), Duration.ofMillis(1)] || ScheduledExpiringMap
    }

}
