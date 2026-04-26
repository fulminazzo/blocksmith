//file:noinspection unused
package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.annotation.Command
import it.fulminazzo.blocksmith.command.annotation.Permission
import it.fulminazzo.blocksmith.command.node.ArgumentNode
import it.fulminazzo.blocksmith.command.node.info.CommandInfo
import it.fulminazzo.blocksmith.command.node.handler.ExecutionHandler
import it.fulminazzo.blocksmith.command.node.LiteralNode
import it.fulminazzo.blocksmith.command.node.info.PermissionInfo
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class CommandRegistryTest extends Specification {
    private MockCommandRegistry registry

    void setup() {
        registry = new MockCommandRegistry()
    }

    def 'test that insert correctly inserts commands after registration'() {
        when:
        registry.register(Commands).commit().insert(new ClanCommand())

        then:
        noExceptionThrown()

        and:
        registry.state == CommandRegistry.State.REGISTERED

        and:
        registry.commands['help'] != null

        and:
        def help = registry.registeredCommands['help']
        help != null
        help.permission.permission == 'blocksmith.help'

        and:
        registry.commands['clan'] != null

        and:
        def clan = registry.registeredCommands['clan']
        clan != null
        clan.permission.permission == 'custom.edited.clan'
    }

    def 'test that insert throws if command is already added'() {
        when:
        registry.register(Commands).commit().insert(Commands)

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that insert throws for state #state'() {
        given:
        registry.state = state

        when:
        registry.insert()

        then:
        thrown(IllegalStateException)

        where:
        state << [CommandRegistry.State.REGISTERING, CommandRegistry.State.INITIAL]
    }

    def 'test that register correctly merges command modules'() {
        given:
        def clanAdminExecutor = new ClanAdminCommand()
        def clanExecutor = new ClanCommand()
        def expected = [:]

        def clan = new LiteralNode('clan', 'team', 'gang')
        clan.executor = new ExecutionHandler(
                clanExecutor,
                ClanCommand.getMethod('execute')
        )
        clan.commandInfo = new CommandInfo(
                'Clan base command',
                new PermissionInfo('custom', 'edited.clan', Permission.Grant.ALL)
        )
        expected['clan'] = clan

        def admin = new LiteralNode('admin')
        admin.executor = new ExecutionHandler(
                clanExecutor,
                ClanCommand.getMethod('admin')
        )
        admin.commandInfo = new CommandInfo(
                'Clan admin command',
                new PermissionInfo('blocksmith', 'clan.admin', Permission.Grant.OP)
        )
        clan.addChild(admin)

        def invite = new LiteralNode('invite')
        invite.commandInfo = new CommandInfo(
                'command.clan.admin.invite.description',
                new PermissionInfo('blocksmith', 'clan.admin.invite', Permission.Grant.OP)
        )
        admin.addChild(invite)

        def method = ClanAdminCommand.getMethod('adminInvite', CommandSender, Object)
        def target = ArgumentNode.of('target', method.parameters[1], false)
        target.executor = new ExecutionHandler(
                clanAdminExecutor,
                method
        )
        invite.addChild(target)

        def members = new LiteralNode('members')
        members.executor = new ExecutionHandler(
                clanAdminExecutor,
                ClanAdminCommand.getMethod('adminMembers', CommandSender)
        )
        members.commandInfo = new CommandInfo(
                'command.clan.admin.members.description',
                new PermissionInfo('blocksmith', 'clan.admin.members', Permission.Grant.ALL)
        )
        admin.addChild(members)

        def kick = new LiteralNode('kick')
        kick.commandInfo = new CommandInfo(
                'command.clan.admin.members.kick.description',
                new PermissionInfo('blocksmith', 'clan.admin.members.kick', Permission.Grant.OP)
        )
        members.addChild(kick)

        method = ClanAdminCommand.getMethod('adminMembersKick', CommandSender, Object)
        target = ArgumentNode.of('target', method.parameters[1], false)
        target.executor = new ExecutionHandler(
                clanAdminExecutor,
                method
        )
        kick.addChild(target)

        when:
        registry.register(clanAdminExecutor).register(clanExecutor)

        then:
        registry.commands == expected

        when:
        registry.commands.clear()

        and:
        registry.register(clanExecutor).register(clanAdminExecutor)

        then:
        registry.commands == expected
    }

    def 'test that register does not call onRegister but stores command for later registration'() {
        when:
        registry.register(Commands)

        then:
        noExceptionThrown()

        and:
        registry.state == CommandRegistry.State.REGISTERING

        and:
        registry.commands['help']
        registry.registeredCommands['help'] == null
    }

    def 'test that register throws if already registered'() {
        given:
        registry.state = CommandRegistry.State.REGISTERED

        when:
        registry.register(Commands)

        then:
        thrown(IllegalStateException)
    }

    def 'test that commit registers command'() {
        when:
        registry.register(Commands).commit()

        then:
        noExceptionThrown()

        and:
        registry.state == CommandRegistry.State.REGISTERED

        and:
        registry.commands['help'] != null

        and:
        def command = registry.registeredCommands['help']
        command != null
        command.permission.permission == 'blocksmith.help'
    }

    def 'test that commit throws for state #state'() {
        given:
        registry.state = state

        when:
        registry.commit()

        then:
        thrown(IllegalStateException)

        where:
        state << [CommandRegistry.State.REGISTERED, CommandRegistry.State.INITIAL]
    }

    def 'test that unregisterAll works'() {
        when:
        registry.register(Commands).commit().unregisterAll()

        then:
        noExceptionThrown()

        and:
        registry.state == CommandRegistry.State.INITIAL

        and:
        registry.commands.isEmpty()
        registry.registeredCommands.isEmpty()
    }

    def 'test that unregisterAll throws for state #state'() {
        given:
        registry.state = state

        when:
        registry.unregisterAll()

        then:
        thrown(IllegalStateException)

        where:
        state << [CommandRegistry.State.REGISTERING, CommandRegistry.State.INITIAL]
    }

    def 'test that unregister does not throw if not present'() {
        when:
        registry.unregister('unknown')

        then:
        noExceptionThrown()
    }

    static final class Commands {

        @Command('help')
        static void help(final @NotNull CommandSender sender) {

        }

    }

}
