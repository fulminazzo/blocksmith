package it.fulminazzo.blocksmith.structure.expiring

import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors

class ExpiringListTest extends Specification {

    def 'test that #method with #arguments returns #type list'() {
        when:
        def list = ExpiringList."$method"(*arguments)

        then:
        (list.getClass() == type)

        where:
        method      | arguments                                                            || type
        'passive'   | []                                                                   || PassiveExpiringList
        'lazy'      | []                                                                   || LazyExpiringList
        'scheduled' | [Executors.newSingleThreadScheduledExecutor(), Duration.ofMillis(1)] || ScheduledExpiringList
    }

}
