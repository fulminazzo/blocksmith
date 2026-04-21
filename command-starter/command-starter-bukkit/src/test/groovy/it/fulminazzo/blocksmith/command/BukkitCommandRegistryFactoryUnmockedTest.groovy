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
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

/**
 * These tests compensate for the errors introduced by MockBukkit in {@link BukkitCommandRegistryFactoryTest}.
 */
class BukkitCommandRegistryFactoryUnmockedTest extends Specification {

    def 'test newRegistry in newer versions returns brigadier registry'() {
        given:
        def server = Spy(CraftServer, additionalInterfaces: [Server])
        Reflect.on(server).set('map', new SimpleCommandMap(server as Server))
        server.handle.commandDispatcher = new CommandDispatcher<>()
        server.pluginManager >> Mock(PluginManager)

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
        def server = Spy(CraftServer, additionalInterfaces: [Server])
        Reflect.on(server).set('map', new SimpleCommandMap(server as Server))
        server.pluginManager >> Mock(PluginManager)

        and:
        def application = Mock(ApplicationHandle)
        application.server() >> server

        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        (registry instanceof BukkitCommandRegistry)
    }

    def 'test that convert of Position to Location with no worlds does not throw'() {
        given:
        def server = Mock(Server)
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

    @SuppressWarnings('unused')
    private static class CraftServer {
        private final ServerHandle handle = new ServerHandle()
        private SimpleCommandMap map

        ServerHandle getHandle() {
            return handle
        }

    }

    private static final class ServerHandle {
        private CommandDispatcher<?> commandDispatcher

    }

}
