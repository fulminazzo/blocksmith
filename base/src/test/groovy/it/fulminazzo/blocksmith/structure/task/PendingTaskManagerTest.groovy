package it.fulminazzo.blocksmith.structure.task

import spock.lang.Specification

import java.time.Duration

class PendingTaskManagerTest extends Specification {
    private boolean executed = false

    def 'test that PendingTaskManager#execute works'() {
        given:
        def manager = new PendingTaskManager()

        and:
        def target = new Object()

        when:
        def result = manager.execute(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND

        when:
        manager.register(target, Duration.ofSeconds(10), () -> executed = true)

        then:
        noExceptionThrown()

        when:
        result = manager.execute(target)

        then:
        result == PendingTaskManager.Result.SUCCESS

        and:
        executed

        when:
        result = manager.execute(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND

        when:
        executed = false

        and:
        manager.register(target, Duration.ofMillis(1), () -> executed = true)

        then:
        noExceptionThrown()

        when:
        sleep(10)

        and:
        result = manager.execute(target)

        then:
        result == PendingTaskManager.Result.EXPIRED

        and:
        !executed

        when:
        result = manager.execute(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND
    }

    def 'test that PendingTaskManager#cancel works'() {
        given:
        def manager = new PendingTaskManager()

        and:
        def target = new Object()

        when:
        def result = manager.cancel(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND

        when:
        manager.register(target, Duration.ofSeconds(10), () -> canceld = true)

        then:
        noExceptionThrown()

        when:
        result = manager.cancel(target)

        then:
        result == PendingTaskManager.Result.SUCCESS

        when:
        result = manager.cancel(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND

        and:
        manager.register(target, Duration.ofMillis(1), () -> canceld = true)

        then:
        noExceptionThrown()

        when:
        sleep(10)

        and:
        result = manager.cancel(target)

        then:
        result == PendingTaskManager.Result.EXPIRED

        when:
        result = manager.cancel(target)

        then:
        result == PendingTaskManager.Result.NOT_FOUND
    }

}
