package it.fulminazzo.blocksmith.structure.expiring

import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors

class ExpiringSetTest extends Specification {

    def 'test that #method with #arguments works'() {
        when:
        def set = ExpiringSet."$method"(*arguments)

        then:
        set != null

        where:
        method      | arguments
        'passive'   | []
        'lazy'      | []
        'scheduled' | [Executors.newSingleThreadScheduledExecutor(), Duration.ofMillis(1)]
    }

}
