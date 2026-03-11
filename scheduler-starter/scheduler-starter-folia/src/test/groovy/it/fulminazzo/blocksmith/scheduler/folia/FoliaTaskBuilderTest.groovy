package it.fulminazzo.blocksmith.scheduler.folia

import be.seeseemelk.mockbukkit.MockBukkit
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import spock.lang.Specification

import java.time.Duration

class FoliaTaskBuilderTest extends Specification {

    void setupSpec() {
        MockBukkit.mock()
    }

    void cleanupSpec() {
        MockBukkit.unmock()
    }

    def 'test that run with #delay, #interval and #async calls on #expectedMethod'() {
        given:
        def scheduler = Mock(async ? AsyncScheduler : GlobalRegionScheduler)
        def internal = Mock(ScheduledTask)

        and:
        def owner = Mock(Plugin)
        owner.server >> {
            def server = Mock(Server)
            if (async) server.asyncScheduler >> scheduler
            else server.globalRegionScheduler >> scheduler
            return server
        }

        and:
        def builder = new FoliaTaskBuilder(owner, (t) -> {
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
            a[1].accept(null)
            return internal
        }

        and:
        task != null
        task.internal == internal

        where:
        delay                 | interval              | async || expectedMethod
        null                  | null                  | false || 'run'
        null                  | null                  | true  || 'runNow'
        Duration.ofMillis(1L) | null                  | false || 'runDelayed'
        Duration.ofMillis(1L) | null                  | true  || 'runDelayed'
        null                  | Duration.ofMillis(1L) | false || 'runAtFixedRate'
        null                  | Duration.ofMillis(1L) | true  || 'runAtFixedRate'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | false || 'runAtFixedRate'
        Duration.ofMillis(1L) | Duration.ofMillis(1L) | true  || 'runAtFixedRate'
    }

}
