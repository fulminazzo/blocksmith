package it.fulminazzo.blocksmith.scheduler.bungee

import net.md_5.bungee.api.scheduler.ScheduledTask
import spock.lang.Specification

class BungeeTaskTest extends Specification {

    private ScheduledTask internal

    void setup() {
        internal = Mock(ScheduledTask)
    }

    def 'test that cancelled works'() {
        given:
        def task = new BungeeTask()
        task.internal = internal

        expect:
        !task.cancelled

        when:
        task.cancel()

        then:
        task.cancelled
    }

    def 'test that async always returns true'() {
        given:
        def task = new BungeeTask()

        expect:
        task.async
    }

    def 'test #method calls #internalMethod on internal'() {
        given:
        def task = new BungeeTask()
        task.internal = internal

        when:
        task."$method"()

        then:
        1 * internal."$internalMethod"()

        where:
        method        || internalMethod
        'cancel'      || 'cancel'
        'getOwner'    || 'getOwner'
    }

    def 'test getInternal throws if not set'() {
        given:
        def task = new BungeeTask()

        when:
        task.internal

        then:
        thrown(IllegalStateException)
    }

}
