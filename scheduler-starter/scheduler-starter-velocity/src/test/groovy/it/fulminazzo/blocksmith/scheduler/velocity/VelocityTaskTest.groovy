package it.fulminazzo.blocksmith.scheduler.velocity

import com.velocitypowered.api.scheduler.ScheduledTask
import com.velocitypowered.api.scheduler.TaskStatus
import spock.lang.Specification

class VelocityTaskTest extends Specification {

    private ScheduledTask internal

    void setup() {
        internal = Mock(ScheduledTask)
    }

    def 'test that getOwner returns owner'() {
        given:
        def task = new VelocityTask(this)

        expect:
        task.owner == this
    }

    def 'test that async always returns true'() {
        given:
        def task = new VelocityTask(this)

        expect:
        task.async
    }

    def 'test that isCancelled returns #expected for #status'() {
        given:
        def task = new VelocityTask(this)
        task.internal = internal
        internal.status() >> status

        when:
        def actual = task.cancelled

        then:
        actual == expected

        where:
        status               || expected
        TaskStatus.CANCELLED || true
        TaskStatus.FINISHED  || false
        TaskStatus.SCHEDULED || false
    }

    def 'test #method calls #internalMethod on internal'() {
        given:
        def task = new VelocityTask(this)
        task.internal = internal

        when:
        task."$method"()

        then:
        1 * internal."$internalMethod"()

        where:
        method        || internalMethod
        'cancel'      || 'cancel'
        'isCancelled' || 'status'
    }

    def 'test getInternal throws if not set'() {
        given:
        def task = new VelocityTask(this)

        when:
        task.internal

        then:
        thrown(IllegalStateException)
    }

}
