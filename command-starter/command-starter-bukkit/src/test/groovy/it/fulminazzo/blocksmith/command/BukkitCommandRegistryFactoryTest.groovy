package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.OfflinePlayerMock
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.Visitor
import it.fulminazzo.blocksmith.reflect.ReflectException
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class BukkitCommandRegistryFactoryTest extends Specification {

    private ApplicationHandle application

    private Visitor<?, ? extends Exception> visitor

    void setupSpec() {
        MockBukkit.mock()

        ServerMock server = Bukkit.server as ServerMock

        server.addPlayer('Alex')
        server.addPlayer('Camilla')
        server.playerList.addOfflinePlayer(new OfflinePlayerMock('Steve'))
        server.playerList.addOfflinePlayer(new OfflinePlayerMock('Michael'))

        server.addSimpleWorld('world_nether')
    }

    void cleanupSpec() {
        MockBukkit.unmock()
    }

    void setup() {
        application = Mock(Plugin, additionalInterfaces: [ApplicationHandle]) as ApplicationHandle
        application.server >> Bukkit.server
        application.server() >> {
            return application.server
        }

        def player = Mock(Player)
        player.location >> new Location(null, 1, 0, 6)
        player.canSee(_) >> true

        visitor = newVisitor(player)
    }

    def 'test that newCommandRegistry returns BungeeCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(application)

        then:
        thrown(ReflectException) // ServerMock does not have getHandle method
//        (registry instanceof BukkitCommandRegistry)
    }

    def 'test parse of parser for Player with Player sender does not throw if they can see'() {
        given:
        def parser = ArgumentParsers.of(Player)
        def argument = 'Camilla'

        and:
        def server = Bukkit.server
        def sender = server.getPlayer('Alex')
        sender.showPlayer(application, server.getPlayer(argument))

        and:
        def visitor = newVisitor(sender)

        when:
        visitor.input.addInput(argument)
        def actual = parser.parse(visitor)

        then:
        actual == server.getPlayer('Camilla')
    }

    def 'test parse of parser for Player with Player sender that can see = #canSee and #argument throws'() {
        given:
        def parser = ArgumentParsers.of(Player)

        and:
        def server = Bukkit.server as ServerMock
        def sender = server.getPlayer('Alex')

        def target = server.getPlayer(argument)
        if (target != null) {
            if (canSee) sender.showPlayer(application, target)
            else sender.hidePlayer(application, target)
        }

        and:
        def visitor = newVisitor(sender)

        when:
        visitor.input.addInput(argument)
        parser.parse(visitor)

        then:
        def e = thrown(ArgumentParseException)
        e.message == 'error.player-not-found'

        where:
        canSee | argument
        true   | 'Steve'
        false  | 'Steve'
        false  | 'Camilla'
    }

    def 'test that completions of parser for Player returns #expected when can see = #canSee'() {
        given:
        def parser = ArgumentParsers.of(Player)

        and:
        def server = Bukkit.server as ServerMock
        def sender = server.getPlayer('Alex')

        def target = server.getPlayer('Camilla')
        if (canSee) sender.hiddenPlayers.clear()
        else sender.hidePlayer(application, target)

        and:
        def visitor = newVisitor(sender)

        when:
        visitor.input.addInput('')
        def actual = parser.getCompletions(visitor)

        then:
        actual == expected

        where:
        canSee || expected
        false  || ['Alex']
        true   || ['Alex', 'Camilla']
    }

    def 'test that parse of parser for #type returns #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        when:
        visitor.input.addInput(argument.split(' '))
        def actual = parser.parse(visitor)

        then:
        actual == expected(application)

        where:
        type          | argument       || expected
        // PLAYER
        Player        | 'Alex'         || { a -> a.server().getPlayer('Alex') }
        Player        | 'Camilla'      || { a -> a.server().getPlayer('Camilla') }
        // OFFLINE PLAYER
        OfflinePlayer | 'Alex'         || { a -> a.server().getOfflinePlayer('Alex') }
        OfflinePlayer | 'Camilla'      || { a -> a.server().getOfflinePlayer('Camilla') }
        OfflinePlayer | 'Steve'        || { a -> a.server().getOfflinePlayer('Steve') }
        OfflinePlayer | 'Michael'      || { a -> a.server().getOfflinePlayer('Michael') }
        // WORLD
        World         | 'world'        || { a -> a.server().getWorld('world') }
        World         | 'world_nether' || { a -> a.server().getWorld('world_nether') }
        // LOCATION
        Location      | '1 2 3'        || { a -> new Location(null, 1, 2, 3) }
        //TODO: re-introduce
//        Location      | '~ ~2 ~-3'     || { a -> new Location(null, 1, 2, 3) }
    }

    def 'test that parse of parser for #type throws exception with #expected message with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        when:
        visitor.input.addInput(argument.split(' '))
        parser.parse(visitor)

        then:
        def e = thrown(ArgumentParseException)
        e.message == expected

        where:
        type          | argument        || expected
        // PLAYER
        Player        | 'z'             || 'error.player-not-found'
        Player        | 'steve'         || 'error.player-not-found'
        // OFFLINE PLAYER
        OfflinePlayer | ''              || 'error.player-not-found'
        OfflinePlayer | 'z'             || 'error.player-not-found'
        OfflinePlayer | 'jake'          || 'error.player-not-found'
        // WORLD
        World         | ''              || 'error.world-not-found'
        World         | 'l'             || 'error.world-not-found'
        World         | 'm'             || 'error.world-not-found'
        World         | 'M'             || 'error.world-not-found'
        World         | 'world_the_end' || 'error.world-not-found'
        // LOCATION
        Location      | ''              || 'error.invalid-number'
        Location      | '1'             || 'error.not-enough-arguments'
        Location      | '1 2'           || 'error.not-enough-arguments'
        Location      | '1 2 a'         || 'error.invalid-number'
        //TODO: re-introduce
//        Location      | '~ ~2 a'        || 'error.invalid-number'
        Location      | 'a'             || 'error.invalid-number'
    }

    def 'test that completions of parser for #type return #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        when:
        visitor.input.addInput(argument.split(' '))
        def actual = parser.getCompletions(visitor)

        then:
        actual.sort() == expected.sort()

        where:
        type          | argument        || expected
        // PLAYER
        Player        | ''              || ['Alex', 'Camilla']
        Player        | 'A'             || ['Alex', 'Camilla']
        Player        | 'Alex'          || ['Alex', 'Camilla']
        Player        | 'C'             || ['Alex', 'Camilla']
        Player        | 'Camilla'       || ['Alex', 'Camilla']
        Player        | 'c'             || ['Alex', 'Camilla']
        Player        | 'steve'         || ['Alex', 'Camilla']
        // OFFLINE PLAYER
        OfflinePlayer | ''              || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'A'             || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'Alex'          || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'C'             || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'Camilla'       || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'c'             || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'steve'         || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer | 'Jake'          || ['Alex', 'Camilla', 'Steve', 'Michael']
        // WORLD
        World         | ''              || ['world', 'world_nether']
        World         | 'l'             || ['world', 'world_nether']
        World         | 'world'         || ['world', 'world_nether']
        World         | 'm'             || ['world', 'world_nether']
        World         | 'world_nether'  || ['world', 'world_nether']
        World         | 'M'             || ['world', 'world_nether']
        World         | 'world_the_end' || ['world', 'world_nether']
        // LOCATION
        Location      | ''              || (0..9).collect { "$it" }
        Location      | '1'             || (0..9).collect { "1$it" }
        Location      | '1 2'           || (0..9).collect { "2$it" }
        Location      | '1 2 3'         || (0..9).collect { "3$it" }
        //TODO: re-introduce
//        Location      | '~ ~2 ~-3'      || ['<x> <y> <z>']
        Location      | 'a'             || []
    }

    private Visitor<?, ? extends Exception> newVisitor(final CommandSender sender) {
        def input = new CommandInput()
        def visitor = Mock(Visitor)
        visitor.application >> application
        visitor.input >> input
        visitor.commandSender >> new BukkitCommandSenderWrapper(application, sender)
        return visitor
    }

}
