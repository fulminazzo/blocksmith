package it.fulminazzo.blocksmith.action

import spock.lang.Specification

import java.time.Duration

class PendingActionManagerTest extends Specification {

    private boolean executed = false

    def 'test that PendingActionManager#execute works'() {
        given:
        def manager = new PendingActionManager()

        and:
        def target = new Object()

        when:
        def result = manager.execute(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND

        when:
        manager.register(target, Duration.ofSeconds(10), () -> executed = true)

        then:
        noExceptionThrown()

        when:
        result = manager.execute(target)

        then:
        result == PendingActionManager.Result.SUCCESS

        and:
        executed

        when:
        result = manager.execute(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND

        when:
        executed = false

        and:
        manager.register(target, Duration.ofNanos(1), () -> executed = true)

        then:
        noExceptionThrown()

        when:
        result = manager.execute(target)

        then:
        result == PendingActionManager.Result.EXPIRED

        and:
        !executed

        when:
        result = manager.execute(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND
    }

    def 'test that PendingActionManager#cancel works'() {
        given:
        def manager = new PendingActionManager()

        and:
        def target = new Object()

        when:
        def result = manager.cancel(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND

        when:
        manager.register(target, Duration.ofSeconds(10), () -> canceld = true)

        then:
        noExceptionThrown()

        when:
        result = manager.cancel(target)

        then:
        result == PendingActionManager.Result.SUCCESS

        when:
        result = manager.cancel(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND

        and:
        manager.register(target, Duration.ofNanos(1), () -> canceld = true)

        then:
        noExceptionThrown()

        when:
        result = manager.cancel(target)

        then:
        result == PendingActionManager.Result.EXPIRED

        when:
        result = manager.cancel(target)

        then:
        result == PendingActionManager.Result.NOT_FOUND
    }

}
