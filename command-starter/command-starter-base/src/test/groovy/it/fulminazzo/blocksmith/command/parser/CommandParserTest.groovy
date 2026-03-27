//file:noinspection unused
package it.fulminazzo.blocksmith.command.parser

import it.fulminazzo.blocksmith.command.CommandSender
import it.fulminazzo.blocksmith.command.annotation.*
import it.fulminazzo.blocksmith.command.node.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import spock.lang.Specification

import java.time.Duration

class CommandParserTest extends Specification {

    def 'test parseCommands returns all commands'() {
        given:
        def executor = new ClanCommand()
        def expected = []
        def baseAliases = ['clan', 'team', 'gang']

        and:
        def clan = new LiteralNode(*baseAliases)
        clan.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('execute', CommandSender)
        )
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.info', Permission.Grant.OP)
        )
        info.addChild(name)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.help', Permission.Grant.ALL)
        )
        help.addChild(verbose)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.admin', Permission.Grant.OP)
        )
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.admin.invite', Permission.Grant.OP)
        )
        invite.addChild(target)
        admin = new LiteralNode('admin')
        admin.commandInfo = new CommandInfo(
                'command.description.clan.admin',
                new PermissionInfo(null, 'clan.admin', Permission.Grant.OP)
        )
        admin.addChild(invite)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.admin.members', Permission.Grant.ALL)
        )
        members.addChild(target)
        admin = new LiteralNode('admin')
        admin.commandInfo = new CommandInfo(
                'command.description.clan.admin',
                new PermissionInfo(null, 'clan.admin', Permission.Grant.OP)
        )
        admin.addChild(members)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
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
                new PermissionInfo(null, 'clan.admin.members.kick', Permission.Grant.OP)
        )
        kick.addChild(target)
        members = new LiteralNode('members')
        members.commandInfo = new CommandInfo(
                'command.description.clan.admin.members',
                new PermissionInfo(null, 'clan.admin.members', Permission.Grant.OP)
        )
        members.addChild(kick)
        admin = new LiteralNode('admin')
        admin.commandInfo = new CommandInfo(
                'command.description.clan.admin',
                new PermissionInfo(null, 'clan.admin', Permission.Grant.OP)
        )
        admin.addChild(members)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
        clan.addChild(admin)
        expected.add(clan)

        and:
        def edit = new LiteralNode('edit')
        edit.commandInfo = new CommandInfo(
                'command.description.clan.admin.gui.edit',
                new PermissionInfo(null, 'clan.admin.gui.edit', Permission.Grant.OP)
        )
        def gui = new LiteralNode('gui')
        gui.commandInfo = new CommandInfo(
                'command.description.clan.admin.gui',
                new PermissionInfo(null, 'clan.admin.gui', Permission.Grant.OP)
        )
        gui.addChild(edit)
        admin = new LiteralNode('admin')
        admin.commandInfo = new CommandInfo(
                'command.description.clan.admin',
                new PermissionInfo(null, 'clan.admin', Permission.Grant.OP)
        )
        admin.addChild(members)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
        clan.addChild(admin)
        expected.add(clan)

        when:
        def actual = CommandParser.parseCommands(executor, CommandSender, null)

        then:
        actual.sort { getCommandName(it) } == expected.sort { getCommandName(it) }
    }

    def 'test parseCommands of dynamic command'() {
        given:
        def executor = new DynamicClanCommand()
        def expected = []
        def baseAliases = ['clan', 'team', 'gang']

        and:
        def clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
        def value = new ArgumentNode('value', double, false)
        value.executionInfo = new ExecutionInfo(
                executor,
                ClanCommand.getMethod('execute', CommandSender)
        )
        clan.addChild(value)
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
                new PermissionInfo(null, 'clan.help', Permission.Grant.ALL)
        )
        help.addChild(verbose)
        clan = new LiteralNode(*baseAliases)
        clan.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
        clan.addChild(help)
        expected.add(clan)

        when:
        def actual = CommandParser.parseCommands(executor, CommandSender, null)

        then:
        actual.sort { getCommandName(it) } == expected.sort { getCommandName(it) }
    }

    def 'test parseCommands throws with #type'() {
        when:
        CommandParser.parseCommands(type.getConstructor().newInstance(), CommandSender, null)

        then:
        thrown(CommandParseException)

        where:
        type << [
                CommandParserTest, CommandNotGiven,
                AliasesNotGiven,
                AliasesNotInstance, AliasesNotCollection,
                AliasesPrivate
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
                new PermissionInfo(null, 'blocksmith.help', Permission.Grant.ALL)
        )

        and:
        def async = new ArgumentNode('async', String, false)
        async.executionInfo = new ExecutionInfo(
                GeneralCommands,
                GeneralCommands.getMethod('reload', boolean)
        )
        async.cooldown = Duration.ofSeconds(1)

        def plugin = new LiteralNode('plugin')
        plugin.addChild(async)
        plugin.commandInfo = new CommandInfo(
                'command.description.reload.plugin',
                new PermissionInfo(null, 'reload.plugin', Permission.Grant.OP)
        )

        def reload = new LiteralNode('reload')
        reload.commandInfo = new CommandInfo(
                'command.description.reload',
                new PermissionInfo(null, 'reload', Permission.Grant.OP)
        )
        reload.addChild(plugin)

        and:
        def expected = [help, reload]

        when:
        def actual = CommandParser.parseCommands(GeneralCommands, CommandSender, null)

        then:
        actual.sort { getCommandName(it) } == expected.sort { getCommandName(it) }
    }

    def 'test parseAnonymousCommands of dynamic commands'() {
        given:
        def help = new LiteralNode('help', '?')
        help.commandInfo = new CommandInfo(
                'command.description.help',
                new PermissionInfo(
                        null,
                        'help',
                        Permission.Grant.OP,
                        true
                ),
                true
        )
        help.executionInfo = new ExecutionInfo(
                DynamicGeneralCommands,
                DynamicGeneralCommands.getMethod('help', CommandSender)
        )

        and:
        def expected = [help]

        when:
        def actual = CommandParser.parseCommands(DynamicGeneralCommands, CommandSender, null)

        then:
        actual == expected
    }

    def 'test that parseAnonymousCommands throws for #type'() {
        when:
        CommandParser.parseAnonymousCommands(type, CommandSender, null)

        then:
        thrown(CommandParseException)

        where:
        type << [
                CommandNotGiven, AliasesNotGiven,
                AliasesNotStatic, AliasesNotCollection,
                AliasesPrivate
        ]
    }

    def 'test that parse works'() {
        given:
        def input = 'clan member <player> (promote|rankup) <rank> [reason]'

        and:
        def commandInfo = new CommandInfo(
                'Example command',
                new PermissionInfo(null, 'clan.member.promote', Permission.Grant.OP)
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
        member.commandInfo = new CommandInfo(
                'command.description.clan.member',
                new PermissionInfo(null, 'clan.member', Permission.Grant.OP)
        )
        member.addChild(player)

        def expected = new LiteralNode('clan')
        expected.commandInfo = new CommandInfo(
                'command.description.clan',
                new PermissionInfo(null, 'clan', Permission.Grant.OP)
        )
        expected.addChild(member)

        and:
        def parser = new CommandParser(input, commandInfo, null, executionInfo, 1, null)

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
                        new PermissionInfo(null, '', Permission.Grant.NONE)
                ),
                null,
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('greedy', String)
                ),
                0,
                null
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
                        new PermissionInfo(null, '', Permission.Grant.NONE)
                ),
                null,
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('promote', Object, Object, String, String)
                ),
                1,
                null
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

        and:
        if (expected instanceof LiteralNode)
            expected.commandInfo = new CommandInfo(
                    'command.description.test',
                    new PermissionInfo(null, 'test', Permission.Grant.OP)
            )

        when:
        def node = parser.parseExpression()

        then:
        node == expected

        where:
        input                 || expected
        '[test]'              || new ArgumentNode('test', String, true)
        '<test>'              || new ArgumentNode('test', String, false)
        '(test|second|third)' || new LiteralNode('test', 'second', 'third')
        'test'                || new LiteralNode('test')
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

    def 'test that parseGeneralArgument correctly parses range argument'() {
        given:
        def parser = new CommandParser('value',
                new CommandInfo(
                        '',
                        new PermissionInfo(null, '', Permission.Grant.ALL)
                ),
                null,
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('validRange', int)
                ),
                0,
                null
        )
        parser.tokenizer.next()

        when:
        def node = parser.parseGeneralArgument(false)

        then:
        (node instanceof NumberArgumentNode)
        node.min == 0
        node.max == 10
    }

    def 'test that parseGeneralArgument throws for invalid range argument'() {
        given:
        def parser = new CommandParser('value',
                new CommandInfo(
                        '',
                        new PermissionInfo(null, '', Permission.Grant.ALL)
                ),
                null,
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('invalidRange', String)
                ),
                0,
                null
        )
        parser.tokenizer.next()

        when:
        parser.parseGeneralArgument(false)

        then:
        thrown(CommandParseException)
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
                        new PermissionInfo(null, '', Permission.Grant.ALL)
                ),
                null,
                new ExecutionInfo(
                        CommandParserTest,
                        CommandParserTest.getDeclaredMethod('newMockCommandParser', String)
                ),
                0,
                null
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

    private static void validRange(final @NotNull @Range(min = 0, max = 10) int value) {

    }

    private static void invalidRange(final @NotNull @Range(min = 0, max = 10) String value) {

    }

    @Command
    static final class CommandNotGiven {

        @Command
        static void commandNotGiven() {

        }

    }

    @Command(dynamic = true)
    static final class AliasesNotGiven {

        @Command(dynamic = true)
        static void help() {

        }

    }

    static final class AliasesNotStatic {

        @Command(dynamic = true)
        static void help() {

        }

        List<String> getHelpAliases() {
            return []
        }

    }

    @Command(dynamic = true)
    static final class AliasesNotInstance {

        static List<String> getAliases() {
            return []
        }

    }

    @Command(dynamic = true)
    static final class AliasesNotCollection {

        @Command(dynamic = true)
        static void help() {

        }

        static String getHelpAliases() {
            return 'Hello, world!'
        }

        String getAliases() {
            return 'Hello, world!'
        }

    }

    @Command(dynamic = true)
    static final class AliasesPrivate {

        @Command(dynamic = true)
        static void help() {

        }

        private static List<String> getHelpAliases() {
            return []
        }

        private List<String> getAliases() {
            return []
        }

    }

}
