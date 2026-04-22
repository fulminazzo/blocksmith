package it.fulminazzo.blocksmith.command.argument

import it.fulminazzo.blocksmith.command.visitor.CommandInput
import it.fulminazzo.blocksmith.command.visitor.InputVisitor
import spock.lang.Specification

class DelegateArgumentParserTest extends Specification {

    def 'test that parse works'() {
        given:
        def visitor = Mock(InputVisitor)
        visitor.input >> {
            def input = Mock(CommandInput)
            input.current >> 'Hello, world!'
            return input
        }

        and:
        def parser = new DelegateArgumentParser((v, s) -> "$s", String)

        when:
        def actual = parser.parse(visitor)

        then:
        actual == "Hello, world!"
        (actual instanceof GString)
    }

    def 'test that #method delegates to delegate'() {
        given:
        def delegate = Mock(ArgumentParser)

        and:
        def parser = new DelegateArgumentParser((v, f) -> null, delegate)

        and:
        def visitor = Mock(InputVisitor)

        when:
        parser."$method"(visitor)

        then:
        1 * delegate."$method"(visitor)

        where:
        method << ['tryAdvanceCursor', 'getCompletions']
    }

}
