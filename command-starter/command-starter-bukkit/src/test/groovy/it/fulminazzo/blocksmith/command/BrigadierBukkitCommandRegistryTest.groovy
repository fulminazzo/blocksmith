//file:noinspection unused
package it.fulminazzo.blocksmith.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.brigadier.tree.RootCommandNode
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.message.Messenger
import it.fulminazzo.blocksmith.reflect.Reflect
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.help.HelpMap
import org.bukkit.help.HelpTopic
import org.bukkit.plugin.PluginManager
import spock.lang.Specification

@Slf4j
class BrigadierBukkitCommandRegistryTest extends Specification {

    private ApplicationHandle application

    private BrigadierBukkitCommandRegistry registry

    void setup() {
        def helpMap = Spy(CraftHelpMap, additionalInterfaces: [HelpMap]) as HelpMap

        def server = Spy(CraftServer, additionalInterfaces: [Server]) as Server
        Reflect.on(server).set('map', new SimpleCommandMap(server))
        server.pluginManager >> Mock(PluginManager)
        server.helpMap >> helpMap

        application = Mock(ApplicationHandle)
        application.server() >> server
        application.logger() >> log
        application.messenger >> new Messenger(application)
        application.name >> 'blocksmith'

        registry = new BrigadierBukkitCommandRegistry(application, Mock(CommandDispatcher))
    }

    def 'test that removeChild removes both children and literals'() {
        given:
        def root = new RootCommandNode<>()
        (1..10).collect {
            def node = Mock(LiteralCommandNode)
            node.name >> "node$it"
            return node
        }.each { root.addChild(it) }

        and:
        def reflect = Reflect.on(root)
        Map<String, CommandNode<?>> children = reflect.get('children').get()
        Map<String, LiteralCommandNode<?>> literals = reflect.get('literals').get()

        and:
        Reflect.on(registry).set('cachedRoot', root)

        expect:
        for (def i in 1..10) {
            final name = "node$i"

            assert root.getChild(name) != null
            assert children[name] != null
            assert literals[name] != null

            registry.removeChild(name)

            assert root.getChild(name) == null
            assert children[name] == null
            assert literals[name] == null
        }

        and:
        children.isEmpty()
        literals.isEmpty()
    }

    def 'test that updateClientCommands calls update on every player'() {
        given:
        def players = (1..3).collect { Mock(Player) }

        and:
        application.server().onlinePlayers >> players

        when:
        registry.updateClientCommands()

        then:
        1 * players[0].updateCommands()
        1 * players[1].updateCommands()
        1 * players[2].updateCommands()
    }

    def 'test that wrapSender gets internal sender if wrapped'() {
        given:
        def executor = new BrigadierSender(Mock(CommandSender))

        when:
        def wrapped = registry.wrapSender(executor)

        then:
        (wrapped instanceof BukkitCommandSenderWrapper)
        wrapped.actualSender == executor.sender
    }

    def 'test that wrapSender instantiates a new Bukkit CommandSenderWrapper'() {
        given:
        def sender = Mock(CommandSender)

        when:
        def wrapped = registry.wrapSender(sender)

        then:
        (wrapped instanceof BukkitCommandSenderWrapper)
        wrapped.actualSender == sender
    }

    def 'test that sender type is correct'() {
        expect:
        registry.senderType == CommandSender
    }

    private static class CraftServer {
        private final ServerHandle handle = new ServerHandle()
        private SimpleCommandMap map

        void syncCommands() {

        }

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

    private static class BrigadierSender {
        private final CommandSender sender

        BrigadierSender(final CommandSender sender) {
            this.sender = sender
        }

        CommandSender getBukkitSender() {
            return sender
        }

    }

}
