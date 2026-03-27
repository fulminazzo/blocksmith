package it.fulminazzo.blocksmith.scheduler.velocity

import com.velocitypowered.api.proxy.ProxyServer
import it.fulminazzo.blocksmith.scheduler.MockVelocityPlugin
import it.fulminazzo.blocksmith.scheduler.Scheduler
import spock.lang.Specification

class VelocityTaskFactoryTest extends Specification {

    def 'test schedule from scheduler works'() {
        given:
        def run = false

        and:
        def server = Mock(ProxyServer)
        server.scheduler >> {
            def scheduler = Mock(com.velocitypowered.api.scheduler.Scheduler)
            scheduler.buildTask(_, _) >> {a->
                def builder = Mock(com.velocitypowered.api.scheduler.Scheduler.TaskBuilder)
                builder.schedule() >> {
                    a[1].run()
                }
                return builder
            }
            return scheduler
        }
        def plugin = new MockVelocityPlugin(server)

        when:
        def task = Scheduler.schedule(plugin, t -> run = true).run()

        then:
        task != null

        and:
        run
    }

}
