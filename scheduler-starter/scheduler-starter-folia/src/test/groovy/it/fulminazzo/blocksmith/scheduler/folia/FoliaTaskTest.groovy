package it.fulminazzo.blocksmith.scheduler.folia

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import spock.lang.Specification

class FoliaTaskTest extends Specification {

    private ScheduledTask internal

    void setup() {
        internal = Mock(ScheduledTask)
    }

    def 'test that async always returns correct value'() {
        given:
        def task = new FoliaTask(expected)

        expect:
        task.async == expected

        where:
        expected << [true, false]
    }

    def 'test #method calls #internalMethod on internal'() {
        given:
        def task = new FoliaTask(false)
        task.internal = internal

        when:
        task."$method"()

        then:
        1 * internal."$internalMethod"()

        where:
        method        || internalMethod
        'cancel'      || 'cancel'
        'isCancelled' || 'isCancelled'
        'getOwner'    || 'getOwningPlugin'
    }

    def 'test getInternal throws if not set'() {
        given:
        def task = new FoliaTask(false)

        when:
        task.internal

        then:
        thrown(IllegalStateException)
    }

}
