//file:noinspection unused
package it.fulminazzo.blocksmith.command.parser

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.annotation.Command
import it.fulminazzo.blocksmith.command.annotation.Default
import it.fulminazzo.blocksmith.command.annotation.Greedy
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import spock.lang.Specification

class CommandParserTest extends Specification {

    def 'test parseCommands returns all commands'() {
        given:
        def executor = new ClanCommand()
        def expected = []
        def baseAliases = ['clan', 'team', 'gang']

        and:
        def clan = new LiteralNode(*baseAliases)
        expected.add(clan)

        and:
        def name = new ArgumentNode('name', String, true)
        name.defaultValue = 'self'
        name.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('getClanInfo', CommandSender, String)
        )
        def info = new LiteralNode('info', 'information', 'state')
        info.commandInfo = new CommandInfo(
                'Information command',
                new PermissionInfo(
                        'clan.info',
                        Permission.Default.OP
                )
        )
        info.addChild(name)
        clan = new LiteralNode(*baseAliases)
        clan.addChild(info)
        expected.add(clan)

        and:
        def verbose = new ArgumentNode('verbose', boolean, false)
        verbose.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('help', CommandSender, boolean)
        )
        def help = new LiteralNode('help')
        help.commandInfo = new CommandInfo(
                'command.description.clan.help',
                new PermissionInfo(
                        'clan.help',
                        Permission.Default.ALL
                )
        )
        help.addChild(verbose)
        clan = new LiteralNode(*baseAliases)
        clan.addChild(help)
        expected.add(clan)

        and:
        def admin = new LiteralNode('admin')
        admin.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('admin')
        )
        admin.commandInfo = new CommandInfo(
                'command.description.clan.admin',
                new PermissionInfo(
                        'clan.admin',
                        Permission.Default.OP
                )
        )
        clan = new LiteralNode(*baseAliases)
        clan.addChild(admin)
        expected.add(clan)

        and:
        def target = new ArgumentNode('target', String, false)
        target.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('adminInvite', CommandSender, Object)
        )
        def invite = new LiteralNode('invite')
        invite.commandInfo = new CommandInfo(
                'command.description.clan.admin.invite',
                new PermissionInfo(
                        'clan.admin.invite',
                        Permission.Default.OP
                )
        )
        invite.addChild(target)
        admin = new LiteralNode('admin')
        admin.addChild(invite)
        clan = new LiteralNode(*baseAliases)
        clan.addChild(admin)
        expected.add(clan)

        and:
        def members = new LiteralNode('members')
        members.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('adminMembers', CommandSender)
        )
        members.commandInfo = new CommandInfo(
                'command.description.clan.admin.members',
                new PermissionInfo(
                        'clan.admin.members',
                        Permission.Default.ALL
                )
        )
        members.addChild(target)
        admin = new LiteralNode('admin')
        admin.addChild(members)
        clan = new LiteralNode(*baseAliases)
        clan.addChild(admin)
        expected.add(clan)

        and:
        target = new ArgumentNode('target', String, false)
        target.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('adminMembersKick', CommandSender, Object)
        )
        def kick = new LiteralNode('kick')
        kick.commandInfo = new CommandInfo(
                'command.description.clan.admin.members.kick',
                new PermissionInfo(
                        'clan.admin.members.kick',
                        Permission.Default.OP
                )
        )
        kick.addChild(target)
        members = new LiteralNode('members')
        members.addChild(kick)
        admin = new LiteralNode('admin')
        admin.addChild(members)
        clan = new LiteralNode(*baseAliases)
        clan.addChild(admin)
        expected.add(clan)

        when:
        def actual = CommandParser.parseCommands(executor, CommandSender)

        then:
        actual.sort { getCommandName(it) } == expected.sort { getCommandName(it) }
    }

    def 'test parseCommands throws with #type'() {
        when:
        CommandParser.parseCommands(type.getConstructor().newInstance(), CommandSender)

        then:
        thrown(CommandParseException)

        where:
        type << [
                CommandParserTest,
                CommandNotGiven
        ]
    }

    def 'test parseAnonymousCommands returns all commands'() {
        given:
        def command = new ArgumentNode('command', String, true)
        command.executionInfo = new ExecutionInfo(
                GeneralCommands,
                GeneralCommands.getMethod('help', CommandSender, String)
        )

        def help = new LiteralNode('help')
        help.addChild(command)
        help.commandInfo = new CommandInfo(
                'Displays help for all the available commands',
                new PermissionInfo(
                        'blocksmith.help',
                        Permission.Default.ALL
                )
        )

        and:
        def async = new ArgumentNode('async', String, false)
        async.executionInfo = new ExecutionInfo(
                GeneralCommands,
                GeneralCommands.getMethod('reload', boolean)
        )

        def plugin = new LiteralNode('plugin')
        plugin.addChild(async)
        plugin.commandInfo = new CommandInfo(
                'command.description.reload.plugin',
                new PermissionInfo(
                        'reload.plugin',
                        Permission.Default.OP
                )
        )

        def reload = new LiteralNode('reload')
        reload.addChild(plugin)

        and:
        def expected = [help, reload]

        when:
        def actual = CommandParser.parseCommands(GeneralCommands, CommandSender)

        then:
        actual.sort { getCommandName(it) } == expected.sort { getCommandName(it) }
    }

    def 'test that parseAnonymousCommands throws for not given command method'() {
        when:
        CommandParser.parseAnonymousCommands(CommandNotGiven, CommandSender)

        then:
        thrown(CommandParseException)
    }

    def 'test that parse works'() {
        given:
        def input = 'clan member <player> (promote|rankup) <rank> [reason]'

        and:
        def commandInfo = new CommandInfo(
                'Example command',
                new PermissionInfo('clan.member.promote', Permission.Default.OP)
        )

        and:
        def executionInfo = new ExecutionInfo(
                CommandParserTest,
                CommandParserTest.getDeclaredMethod('promote', Object, Object, String, String)
        )

        and:
        def reason = new ArgumentNode('reason', String, true)
        reason.executionInfo = executionInfo
        reason.defaultValue = 'Unknown'

        def rank = new ArgumentNode('rank', String, false)
        rank.addChild(reason)

        def promote = new LiteralNode('promote', 'rankup')
        promote.commandInfo = commandInfo
        promote.addChild(rank)

        def player = new ArgumentNode('player', Object, false)
        player.addChild(promote)

        def member = new LiteralNode('member')
        member.addChild(player)

        def expected = new LiteralNode('clan')
        expected.addChild(member)

        and:
        def parser = new CommandParser(input, commandInfo, executionInfo, 1)

        when:
        def actual = parser.parse()

        then:
        actual == expected
    }

    def 'test that parse throws for nodes after greedy argument'() {
        given:
        def parser = new CommandParser(
                '<argument> something else',
                new CommandInfo(
                        '',
                        new PermissionInfo('', Permission.Default.NONE)
                ),
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('greedy', String)
                ),
                0
        )

        when:
        parser.parse()

        then:
        thrown(CommandParseException)
    }

    def 'test that parse throws for invalid number of arguments and parameters'() {
        given:
        def parser = new CommandParser(
                'clan member <player> (promote|rankup) <rank>',
                new CommandInfo(
                        '',
                        new PermissionInfo('', Permission.Default.NONE)
                ),
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('promote', Object, Object, String, String)
                ),
                1
        )

        when:
        parser.parse()

        then:
        thrown(CommandParseException)
    }

    def 'test that parse throws for #input'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        parser.parse()

        then:
        thrown(CommandParseException)

        where:
        input << [
                '',
                '<<first>'
        ]
    }

    def 'test that parseExpression of #input returns #expected'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        def node = parser.parseExpression()

        then:
        node == expected

        where:
        input                  || expected
        '[test]'               || new ArgumentNode('test', String, true)
        '<test>'               || new ArgumentNode('test', String, false)
        '(first|second|third)' || new LiteralNode('first', 'second', 'third')
        'test'                 || new LiteralNode('test')
    }

    def 'test that parseExpression throws for non-optional argument after optional argument'() {
        given:
        def parser = newMockCommandParser('test')
        parser.optionalArgument = 'optional'

        when:
        parser.parseExpression()

        then:
        def e = thrown(CommandParseException)
        e.message == "Invalid input in command 'test': after declaring optional argument 'optional', all subsequent nodes MUST be of the same kind (optional arguments)"
    }

    def 'test that parseOptionalArgument works'() {
        given:
        def parser = newMockCommandParser('[test]')

        when:
        def node = parser.parseOptionalArgument()

        then:
        node == new ArgumentNode('test', String, true)
    }

    def 'test that parseMandatoryArgument works'() {
        given:
        def parser = newMockCommandParser('<test>')

        when:
        def node = parser.parseMandatoryArgument()

        then:
        node == new ArgumentNode('test', String, false)
    }

    def 'test that parseGeneralArgument throws for non-matched argument'() {
        given:
        def parser = newMockCommandParser('test')

        and:
        parser.parameterIndex++

        when:
        parser.parseGeneralArgument(false)

        then:
        thrown(CommandParseException)
    }

    def 'test that parseAliasesLiteral for #input returns #expected'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        def node = parser.parseAliasesLiteral()

        then:
        node == new LiteralNode(*expected)

        where:
        input                  || expected
        '(first)'              || ['first']
        '(first|second)'       || ['first', 'second']
        '(first|second|third)' || ['first', 'second', 'third']
    }

    def 'test that parseAliasesLiteral throws for #input'() {
        given:
        def parser = newMockCommandParser(input)

        when:
        parser.parseAliasesLiteral()

        then:
        thrown(CommandParseException)

        where:
        input << [
                '(first',
                '(first!',
                '(first!second',
                '(first|second',
                '(first|second!'
        ]
    }

    def 'test that parseSimpleLiteral works'() {
        given:
        def parser = newMockCommandParser('test')

        when:
        def node = parser.parseSimpleLiteral()

        then:
        node == new LiteralNode('test')
    }

    private static CommandParser newMockCommandParser(final @NotNull String input) {
        def parser = new CommandParser(input,
                new CommandInfo(
                        '',
                        new PermissionInfo('', Permission.Default.ALL)
                ),
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('newMockCommandParser', String)
                ),
                0
        )
        parser.tokenizer.next()
        return parser
    }

    private static void promote(final @NotNull Object commandSender,
                                final @NotNull Object player,
                                final @NotNull String rank,
                                final @Nullable @Default('Unknown') String reason) {
    }

    private static void greedy(final @NotNull @Greedy String argument) {

    }

    private static String getCommandName(final @NotNull CommandNode node) {
        def name = ''
        def n = node
        while (n != null) {
            if (n instanceof LiteralNode) name += n.name + '_'
            n = n.firstChild
        }
        return name[0..-2]
    }

    @Command
    static final class CommandNotGiven {

        @Command
        static void commandNotGiven() {

        }

    }

}
