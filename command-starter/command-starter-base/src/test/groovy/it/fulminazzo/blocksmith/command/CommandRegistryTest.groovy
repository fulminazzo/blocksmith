package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.annotation.Command
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class CommandRegistryTest extends Specification {
    private MockCommandRegistry registry

    void setup() {
        registry = new MockCommandRegistry()
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
