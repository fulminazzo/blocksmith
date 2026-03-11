package it.fulminazzo.blocksmith.scheduler.folia

import be.seeseemelk.mockbukkit.MockBukkit
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import it.fulminazzo.blocksmith.scheduler.Scheduler
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import spock.lang.Specification

import java.util.function.Consumer

class FoliaTaskFactoryTest extends Specification {

    void setupSpec() {
        MockBukkit.mock()
    }

    void cleanupSpec() {
        MockBukkit.unmock()
    }

    def 'test schedule from scheduler works'() {
        given:
        def run = false

        and:
        def plugin = Mock(Plugin)
        plugin.server >> {
            def server = Mock(Server)
            server.globalRegionScheduler >> {
                def scheduler = Mock(GlobalRegionScheduler)
                scheduler.run(_ as Plugin, _ as Consumer) >> { a ->
                    a[1].accept(null)
                }
                return scheduler
            }
            return server
        }

        when:
        def task = Scheduler.schedule(plugin, t -> run = true).run()

        then:
        task != null

        and:
        run
    }

    def 'test that schedule with invalid object throws'() {
        when:
        new FoliaTaskFactory().schedule('Hello, world!', t -> {})

        then:
        thrown(IllegalArgumentException)
    }

}
