package it.fulminazzo.blocksmith.scheduler

import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class TaskFactoryTest extends Specification {
    private final static TaskFactory factory = new MockTaskFactory()

    private final List<Long> runs = new ArrayList<>()

    void setup() {
        runs.clear()
    }

    def 'test schedule task runs once'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(0L, t -> addRun()).run()

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 1
        runs.get(0) - now <= 200
    }

    def 'test schedule task runs after delay'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(1L, t -> addRun())
                .delay(1, TimeUnit.SECONDS)
                .run()

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 1
        runs.get(0) - now >= 1000
    }

    def 'test schedule repeating task runs after delay'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(2L, t -> {
            if (runs.size() >= 3) t.cancel()
            else addRun()
        })
                .delay(1, TimeUnit.SECONDS)
                .interval(1, TimeUnit.SECONDS)
                .run()

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 3

        and:
        def first = runs.get(0) - now
        first >= 1000
        first <= 2000

        and:
        def second = runs.get(1) - now
        second >= 2000
        second <= 3000

        and:
        def third = runs.get(2) - now
        third >= 3000
        third <= 4000
    }

    def 'test schedule asynchronous task runs once'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(3L, t -> addRun()).async().run()

        and:
        sleep(500L)

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 1
        runs.get(0) - now <= 200
    }

    def 'test schedule asynchronous task runs after delay'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(4L, t -> addRun())
                .delay(1, TimeUnit.SECONDS)
                .async()
                .run()

        and:
        sleep(1500L)

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 1
        runs.get(0) - now >= 1000
    }

    def 'test schedule asynchronous repeating task runs after delay'() {
        given:
        def now = now

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(5L, t -> {
            if (runs.size() >= 3) t.cancel()
            else addRun()
        })
                .delay(1, TimeUnit.SECONDS)
                .interval(1, TimeUnit.SECONDS)
                .async()
                .run()

        and:
        sleep(5000L)

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 3

        and:
        def first = runs.get(0) - now
        first >= 1000
        first <= 2000

        and:
        def second = runs.get(1) - now
        second >= 2000
        second <= 3000

        and:
        def third = runs.get(2) - now
        third >= 3000
        third <= 4000
    }

    def 'test schedule repeating task with given amount of times runs after delay'() {
        given:
        def now = now
        def times = 0

        expect:
        runs.size() == 0

        when:
        def task = factory.schedule(6L, t -> {
            times++
            addRun()
        })
                .delay(1, TimeUnit.SECONDS)
                .interval(1, TimeUnit.SECONDS)
                .repeated(3)

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 3
        times == 3

        and:
        def first = runs.get(0) - now
        first >= 1000
        first <= 2000

        and:
        def second = runs.get(1) - now
        second >= 2000
        second <= 3000

        and:
        def third = runs.get(2) - now
        third >= 3000
        third <= 4000
    }

    def 'test that runAsyncThen works'() {
        given:
        def value = 0

        expect:
        runs.size() == 0

        when:
        def future = factory.runAsyncThen(
                7L,
                CompletableFuture.supplyAsync(() -> 1),
                n -> {
                    value = 1
                    addRun()
                }
        )

        and:
        def task = future.get()

        then:
        task != null
        task.cancelled

        and:
        runs.size() == 1

        and:
        value == 1
    }

    private void addRun() {
        runs.add(now)
    }

    private static long getNow() {
        return System.currentTimeMillis()
    }

}
