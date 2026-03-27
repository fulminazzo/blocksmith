package it.fulminazzo.blocksmith.scheduler.bukkit

import spock.lang.Specification

class BukkitTaskTest extends Specification {

    private org.bukkit.scheduler.BukkitTask internal

    void setup() {
        internal = Mock(org.bukkit.scheduler.BukkitTask)
    }

    def 'test #method calls #internalMethod on internal'() {
        given:
        def task = new BukkitTask()
        task.internal = internal

        when:
        task."$method"()

        then:
        1 * internal."$internalMethod"()

        where:
        method        || internalMethod
        'cancel'      || 'cancel'
        'isCancelled' || 'isCancelled'
        'getOwner'    || 'getOwner'
        'isAsync'     || 'isSync'
    }

    def 'test getInternal throws if not set'() {
        given:
        def task = new BukkitTask()

        when:
        task.internal

        then:
        thrown(IllegalStateException)
    }

}
