package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import be.seeseemelk.mockbukkit.entity.OfflinePlayerMock
import it.fulminazzo.blocksmith.ApplicationHandle
import it.fulminazzo.blocksmith.command.argument.ArgumentParseException
import it.fulminazzo.blocksmith.command.argument.ArgumentParsers
import it.fulminazzo.blocksmith.command.argument.dto.Coordinate
import it.fulminazzo.blocksmith.command.argument.dto.Position
import it.fulminazzo.blocksmith.command.argument.dto.WorldPosition
import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import it.fulminazzo.blocksmith.reflect.ReflectException
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import spock.lang.Specification

class BukkitCommandRegistryFactoryTest extends Specification {

    private ApplicationHandle application

    private InputVisitor<?, ? extends Exception> visitor

    void setupSpec() {
        MockBukkit.mock()

        ServerMock server = Bukkit.server as ServerMock

        server.addPlayer('Alex').setLocation(new Location(
                server.getWorld('world'),
                0,
                0,
                6
        ))
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

    def 'test parse of parser for Player with console sender does not throw if found'() {
        given:
        def parser = ArgumentParsers.of(Player)
        def argument = 'Camilla'

        and:
        def server = Bukkit.server
        def sender = server.getConsoleSender()

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

    def 'test that completions of parser for Player returns all players when console sender'() {
        given:
        def parser = ArgumentParsers.of(Player)

        and:
        def server = Bukkit.server as ServerMock
        def sender = server.getConsoleSender()

        and:
        def visitor = newVisitor(sender)

        when:
        visitor.input.addInput('')
        def actual = parser.getCompletions(visitor)

        then:
        actual == ['Alex', 'Camilla']
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
        type                 | argument         || expected
        // PLAYER
        Player               | 'Alex'           || { a -> a.server().getPlayer('Alex') }
        Player               | 'Camilla'        || { a -> a.server().getPlayer('Camilla') }
        // CONSOLE
        ConsoleCommandSender | 'console'        || { a -> a.server().consoleSender }
        // COMMAND SENDER
        CommandSender        | 'Alex'           || { a -> a.server().getPlayer('Alex') }
        CommandSender        | 'Camilla'        || { a -> a.server().getPlayer('Camilla') }
        CommandSender        | 'console'        || { a -> a.server().consoleSender }
        // OFFLINE PLAYER
        OfflinePlayer        | 'Alex'           || { a -> a.server().getOfflinePlayer('Alex') }
        OfflinePlayer        | 'Camilla'        || { a -> a.server().getOfflinePlayer('Camilla') }
        OfflinePlayer        | 'Steve'          || { a -> a.server().getOfflinePlayer('Steve') }
        OfflinePlayer        | 'Michael'        || { a -> a.server().getOfflinePlayer('Michael') }
        // WORLD
        World                | 'world'          || { a -> a.server().getWorld('world') }
        World                | 'world_nether'   || { a -> a.server().getWorld('world_nether') }
        // LOCATION
        Location             | 'world 1 2 -3'   || { a -> new Location(a.server().getWorld('world'), 1, 2, -3) }
        Location             | 'world ~ ~2 ~-3' || { a -> new Location(a.server().getWorld('world'), 1, 2, 3) }
        Location             | 'world ~ ~2 ~-4' || { a -> new Location(a.server().getWorld('world'), 1, 2, 2) }
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
        type                 | argument        || expected
        // PLAYER
        Player               | 'z'             || 'error.player-not-found'
        Player               | 'steve'         || 'error.player-not-found'
        // CONSOLE
        ConsoleCommandSender | 'k'             || 'error.unrecognized-argument'
        // COMMAND SENDER
        CommandSender        | 'z'             || 'error.player-not-found'
        CommandSender        | 'steve'         || 'error.player-not-found'
        CommandSender        | 'k'             || 'error.player-not-found'
        // OFFLINE PLAYER
        OfflinePlayer        | ''              || 'error.player-not-found'
        OfflinePlayer        | 'z'             || 'error.player-not-found'
        OfflinePlayer        | 'jake'          || 'error.player-not-found'
        // WORLD
        World                | ''              || 'error.world-not-found'
        World                | 'l'             || 'error.world-not-found'
        World                | 'm'             || 'error.world-not-found'
        World                | 'M'             || 'error.world-not-found'
        World                | 'world_the_end' || 'error.world-not-found'
    }

    def 'test that completions of parser for #type return #expected with #argument'() {
        given:
        def parser = ArgumentParsers.of(type)

        when:
        visitor.input.addInput(argument)
        def actual = [*parser.getCompletions(visitor)]

        then:
        actual.sort() == expected.sort()

        where:
        type                 | argument         || expected
        // PLAYER
        Player               | ''               || ['Alex', 'Camilla']
        Player               | 'A'              || ['Alex', 'Camilla']
        Player               | 'Alex'           || ['Alex', 'Camilla']
        Player               | 'C'              || ['Alex', 'Camilla']
        Player               | 'Camilla'        || ['Alex', 'Camilla']
        Player               | 'c'              || ['Alex', 'Camilla']
        Player               | 'steve'          || ['Alex', 'Camilla']
        // CONSOLE
        ConsoleCommandSender | ''               || [ArgumentParsers.CONSOLE_COMMAND_NAME]
        ConsoleCommandSender | 'c'              || [ArgumentParsers.CONSOLE_COMMAND_NAME]
        ConsoleCommandSender | 'console'        || [ArgumentParsers.CONSOLE_COMMAND_NAME]
        // COMMAND SENDER
        CommandSender        | ''               || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'A'              || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'Alex'           || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'C'              || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'Camilla'        || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'c'              || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'steve'          || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | ''               || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'c'              || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        CommandSender        | 'console'        || [ArgumentParsers.CONSOLE_COMMAND_NAME, 'Alex', 'Camilla']
        // OFFLINE PLAYER
        OfflinePlayer        | ''               || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'A'              || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'Alex'           || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'C'              || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'Camilla'        || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'c'              || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'steve'          || ['Alex', 'Camilla', 'Steve', 'Michael']
        OfflinePlayer        | 'Jake'           || ['Alex', 'Camilla', 'Steve', 'Michael']
        // WORLD
        World                | ''               || ['world', 'world_nether']
        World                | 'l'              || ['world', 'world_nether']
        World                | 'world'          || ['world', 'world_nether']
        World                | 'm'              || ['world', 'world_nether']
        World                | 'world_nether'   || ['world', 'world_nether']
        World                | 'M'              || ['world', 'world_nether']
        World                | 'world_the_end'  || ['world', 'world_nether']
        // LOCATION
        Location             | ''               || ['world', 'world_nether']
        Location             | 'world'          || ['world', 'world_nether']
        Location             | 'world '         || [Coordinate.RELATIVE_IDENTIFIER]
        Location             | 'world 1'        || (0..9).collect { "1$it" }
        Location             | 'world 1 2'      || (0..9).collect { "2$it" }
        Location             | 'world 1 2 3'    || (0..9).collect { "3$it" }
        Location             | 'world ~ ~2 ~-3' || (0..9).collect { "~-3$it" }
        Location             | 'world a'        || []
    }

    def 'test that convert of #position to Location with #sender returns #expected'() {
        given:
        def args = []
        if (sender != null) {
            def s = sender(application)
            args.add(s instanceof CommandSender
                    ? new BukkitCommandSenderWrapper(application, s)
                    : s
            )
        }

        when:
        def actual = position.as(Location, *args)

        then:
        actual == expected(application)

        where:
        position                         | sender                                                       || expected
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | null                                                         ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> 'world' }                                             ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getWorld('world') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getPlayer('Alex') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1.0, 2.0, 3.0) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> new Location(a.server().getWorld('world'), 2, 3, 4) } ||
                { a -> new Location(a.server().getWorld('world'), 1.0, 5.0, 1.0) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> new Object() }                                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().consoleSender }                            ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new Position(
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getPlayer('Alex') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 2, 3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | null                                                         ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> 'world' }                                             ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getWorld('world') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getPlayer('Alex') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1.0, 2.0, 3.0) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> new Location(a.server().getWorld('world'), 2, 3, 4) } ||
                { a -> new Location(a.server().getWorld('world'), 1.0, 5.0, 1.0) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> new Object() }                                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().consoleSender }                            ||
                { a -> new Location(a.server().getWorld('world'), 1, 7.0, -3) }
        new WorldPosition(
                'world',
                new Coordinate(1),
                new Coordinate(2, true),
                new Coordinate(-3, true)
        )                                | { a -> a.server().getPlayer('Alex') }                        ||
                { a -> new Location(a.server().getWorld('world'), 1, 2, 3) }
    }

    private InputVisitor<?, ? extends Exception> newVisitor(final CommandSender sender) {
        def input = new CommandInput()
        def visitor = Mock(InputVisitor)
        visitor.application >> application
        visitor.input >> input
        visitor.commandSender >> new BukkitCommandSenderWrapper(application, sender)
        return visitor
    }

}
