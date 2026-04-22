//file:noinspection unused
package it.fulminazzo.blocksmith.command

import com.mojang.brigadier.CommandDispatcher
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.dto.Coordinate
import it.fulminazzo.blocksmith.command.argument.dto.Position
import it.fulminazzo.blocksmith.reflect.Reflect
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.command.SimpleCommandMap
import org.bukkit.help.HelpMap
import org.bukkit.help.HelpTopic
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

/**
 * These tests compensate for the errors introduced by MockBukkit in {@link BukkitCommandRegistryFactoryTest}.
 */
class BukkitCommandRegistryFactoryUnmockedTest extends Specification {

    private Server server

    void setup() {
        def helpMap = Spy(CraftHelpMap, additionalInterfaces: [HelpMap]) as HelpMap

        server = Spy(CraftServer, additionalInterfaces: [Server])
        Reflect.on(server).set('map', new SimpleCommandMap(server as Server))
        server.pluginManager >> Mock(PluginManager)
        server.helpMap >> helpMap
    }

    def 'test newRegistry in newer versions returns brigadier registry'() {
        given:
        server.handle.commandDispatcher = new CommandDispatcher<>()

        and:
        def application = Mock(ApplicationHandle)
        application.server() >> server

        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        (registry instanceof BrigadierBukkitCommandRegistry)
    }

    def 'test newRegistry in legacy versions returns legacy registry'() {
        given:
        def application = Mock(ApplicationHandle)
        application.server() >> server

        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        (registry instanceof BukkitCommandRegistry)
    }

    def 'test that convert of Position to Location with no worlds does not throw'() {
        given:
        server.worlds >> []
        Reflect.on(Bukkit).set('server', server)

        and:
        def position = new Position(
                new Coordinate(1),
                new Coordinate(2, false),
                new Coordinate(-3, true)
        )

        when:
        def location = position.as(Location)

        then:
        location == new Location(null, 1, 2, -3)

        cleanup:
        Reflect.on(Bukkit).set('server', null)
    }

    private static class CraftServer {
        private final ServerHandle handle = new ServerHandle()
        private SimpleCommandMap map

        ServerHandle getHandle() {
            return handle
        }

    }

    private static class CraftHelpMap {
        private final Map<String, HelpTopic> helpTopics = [:]

    }

    private static final class ServerHandle {
        private CommandDispatcher<?> commandDispatcher

    }

}
