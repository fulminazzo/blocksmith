package it.fulminazzo.blocksmith.scheduler.bukkit

import it.fulminazzo.blocksmith.scheduler.Scheduler
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import spock.lang.Specification

class BukkitTaskFactoryTest extends Specification {

    def 'test schedule from scheduler works'() {
        given:
        def run = false

        and:
        def plugin = Mock(Plugin)
        plugin.server >> {
            def server = Mock(Server)
            server.scheduler >> {
                def scheduler = Mock(BukkitScheduler)
                scheduler.runTask(_ as Plugin, _ as Runnable) >> { a ->
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

    def 'test that schedule with invalid object throws'() {
        when:
        new BukkitTaskFactory().schedule('Hello, world!', t -> {})

        then:
        thrown(IllegalArgumentException)
    }

}
