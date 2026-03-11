package it.fulminazzo.blocksmith.scheduler.folia

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import it.fulminazzo.blocksmith.scheduler.Scheduler
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class FoliaTaskFactoryTest extends Specification {

    def 'test schedule from scheduler works'() {
        given:
        def run = false

        and:
        def plugin = Mock(Plugin)
        plugin.server >> {
            def server = Mock(Server)
            server.globalRegionScheduler >> {
                def scheduler = Mock(GlobalRegionScheduler)
                scheduler.run(_ as Plugin, _ as Runnable) >> { a ->
                    a[1].run()
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

}
