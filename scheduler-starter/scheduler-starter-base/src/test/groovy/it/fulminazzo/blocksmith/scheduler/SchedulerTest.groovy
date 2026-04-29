package it.fulminazzo.blocksmith.scheduler

import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class SchedulerTest extends Specification {

    def 'test that runAsyncThen calls on TaskFactory runAsyncThen'() {
        given:
        def mock = Mock(MockTaskFactory)
        mock.supportsOwner(Long) >> true

        and:
        Scheduler.factories.clear()
        Scheduler.factories.add(mock)

        and:
        def future = CompletableFuture.completedFuture(0)

        and:
        def function = (Consumer<Integer>) (t -> {})

        when:
        Scheduler.runAsyncThen(1L, future, function)

        then:
        1 * mock.runAsyncThen(1L, future, function)

        cleanup:
        Scheduler.factories.clear()
        Scheduler.factories.addAll(ServiceLoader.load(TaskFactory.class))
    }

    def 'test that schedule calls on TaskFactory schedule'() {
        given:
        def mock = Mock(MockTaskFactory)
        mock.supportsOwner(Long) >> true

        and:
        Scheduler.factories.clear()
        Scheduler.factories.add(mock)

        and:
        def function = (Consumer<Task>) (t -> {})

        when:
        Scheduler.schedule(1L, function)

        then:
        1 * mock.schedule(1L, function)

        cleanup:
        Scheduler.factories.clear()
        Scheduler.factories.addAll(ServiceLoader.load(TaskFactory.class))
    }

    def 'test that getFactory returns MockTaskFactory for long'() {
        when:
        def factory = Scheduler.getFactory(Long)

        then:
        (factory instanceof MockTaskFactory)
    }

    def 'test that getFactory throws for unknown owner type'() {
        when:
        Scheduler.getFactory(String)

        then:
        thrown(IllegalArgumentException)
    }

}
