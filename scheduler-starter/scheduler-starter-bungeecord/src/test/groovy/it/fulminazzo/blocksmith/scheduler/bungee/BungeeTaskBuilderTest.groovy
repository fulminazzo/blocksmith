package it.fulminazzo.blocksmith.scheduler.bungee

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import net.md_5.bungee.api.scheduler.TaskScheduler
import spock.lang.Specification

import java.time.Duration

class BungeeTaskBuilderTest extends Specification {

    def 'test that run with #delay, #interval and #async calls on #expectedMethod'() {
        given:
        def scheduler = Mock(TaskScheduler)
        def internal = Mock(ScheduledTask)

        and:
        def owner = Mock(Plugin)
        owner.proxy >> {
            def server = Mock(ProxyServer)
            server.scheduler >> scheduler
            return server
        }

        and:
        def builder = new BungeeTaskBuilder(owner, (t) -> {
            assert t != null
        })
        if (delay != null) builder.delay(delay)
        if (interval != null) builder.interval(interval)

        and:
        if (async) builder.async()

        when:
        def task = builder.run()

        then:
        1 * scheduler."$expectedMethod"(*_) >> { a ->
            a[1].run()
            return internal
        }

        and:
        task != null
        task.internal == internal

        where:
        delay                 | interval              | async || expectedMethod
        null                  | null                  | false || 'runAsync'
        null                  | null                  | true  || 'runAsync'
        Duration.ofMillis(1L) | null                  | false || 'schedule'
        Duration.ofMillis(1L) | null                  | true  || 'schedule'
        null                  | Duration.ofMillis(1L) | false || 'schedule'
        null                  | Duration.ofMillis(1L) | true  || 'schedule'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | false || 'schedule'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | true  || 'schedule'
    }

}
