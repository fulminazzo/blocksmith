package it.fulminazzo.blocksmith.scheduler.velocity

import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import com.velocitypowered.api.scheduler.Scheduler
import it.fulminazzo.blocksmith.scheduler.MockVelocityPlugin
import spock.lang.Specification

import java.time.Duration

class VelocityTaskBuilderTest extends Specification {

    def 'test that run with #delay, #interval and #async calls correct methods'() {
        given:
        def scheduler = Mock(Scheduler)
        def taskBuilder = Mock(Scheduler.TaskBuilder)
        scheduler.buildTask(_, _) >> taskBuilder
        def internal = Mock(ScheduledTask)

        and:
        def server = Mock(ProxyServer)
        server.scheduler >> scheduler
        def owner = new MockVelocityPlugin(server)

        and:
        def builder = new VelocityTaskBuilder(owner, (t) -> {
            assert t != null
        })
        if (delay != null) builder.delay(delay)
        if (interval != null) builder.interval(interval)

        and:
        if (async) builder.async()

        when:
        def task = builder.run()

        then:
        1 * taskBuilder.schedule() >> { internal }

        and:
        if (delay != null) 1 * taskBuilder.delay(delay)

        and:
        if (interval != null) 1 * taskBuilder.repeat(interval)

        and:
        task != null
        task.internal == internal

        where:
        delay                 | interval              | async
        null                  | null                  | false
        null                  | null                  | true
        Duration.ofMillis(1L) | null                  | false
        Duration.ofMillis(1L) | null                  | true
        null                  | Duration.ofMillis(1L) | false
        null                  | Duration.ofMillis(1L) | true
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | false
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | true
    }

}
