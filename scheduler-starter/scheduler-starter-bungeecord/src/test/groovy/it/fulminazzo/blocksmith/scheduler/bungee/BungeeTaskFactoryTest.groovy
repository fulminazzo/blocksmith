package it.fulminazzo.blocksmith.scheduler.bungee

import it.fulminazzo.blocksmith.scheduler.Scheduler
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.TaskScheduler
import spock.lang.Specification

class BungeeTaskFactoryTest extends Specification {

    def 'test schedule from scheduler works'() {
        given:
        def run = false

        and:
        def plugin = Mock(Plugin)
        plugin.proxy >> {
            def server = Mock(ProxyServer)
            server.scheduler >> {
                def scheduler = Mock(TaskScheduler)
                scheduler.runAsync(_ as Plugin, _ as Runnable) >> { a ->
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
