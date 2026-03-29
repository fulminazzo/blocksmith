package it.fulminazzo.blocksmith.scheduler.folia

import be.seeseemelk.mockbukkit.MockBukkit
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler
import io.papermc.paper.threadedregions.scheduler.RegionScheduler
import it.fulminazzo.blocksmith.scheduler.Scheduler
import it.fulminazzo.blocksmith.structure.Pair
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Entity
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

    def 'test schedule from scheduler with pair works'() {
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
            server.regionScheduler >> {
                def scheduler = Mock(RegionScheduler)
                scheduler.run(_ as Plugin, _ as Location, _ as Consumer) >> { a ->
                    a[2].accept(null)
                }
                return scheduler
            }
            return server
        }

        and:
        def location = Mock(Location)

        and:
        def entity = Mock(Entity)
        entity.scheduler >> {
            def scheduler = Mock(EntityScheduler)
            scheduler.run(_ as Plugin, _ as Consumer, _) >> { a ->
                a[1].accept(null)
            }
            return scheduler
        }

        and:
        final second
        if (owner == 'plugin') second = plugin
        else if (owner == 'location') second = location
        else second = entity

        when:
        def task = Scheduler.schedule(Pair.of(plugin, second), t -> run = true).run()

        then:
        task != null

        and:
        run

        where:
        owner << ['plugin', 'location', 'entity']
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
        new FoliaTaskFactory().schedule(owner, t -> { })

        then:
        thrown(IllegalArgumentException)

        where:
        owner << ['Hello, world', Pair.of(null, 'Hello, world')]
    }

}
