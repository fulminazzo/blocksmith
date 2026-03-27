package it.fulminazzo.blocksmith.scheduler.bukkit


import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import spock.lang.Specification

import java.time.Duration

class BukkitTaskBuilderTest extends Specification {

    def 'test that run with #delay, #interval and #async calls on #expectedMethod'() {
        given:
        def scheduler = Mock(BukkitScheduler)
        def internal = Mock(org.bukkit.scheduler.BukkitTask)

        and:
        def owner = Mock(Plugin)
        owner.server >> {
            def server = Mock(Server)
            server.scheduler >> scheduler
            return server
        }

        and:
        def builder = new BukkitTaskBuilder(owner, (t) -> {
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
        null                  | null                  | false || 'runTask'
        null                  | null                  | true  || 'runTaskAsynchronously'
        Duration.ofMillis(1L) | null                  | false || 'runTaskLater'
        Duration.ofMillis(1L) | null                  | true  || 'runTaskLaterAsynchronously'
        null                  | Duration.ofMillis(1L) | false || 'runTaskTimer'
        null                  | Duration.ofMillis(1L) | true  || 'runTaskTimerAsynchronously'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | false || 'runTaskTimer'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | true  || 'runTaskTimerAsynchronously'
    }

}
