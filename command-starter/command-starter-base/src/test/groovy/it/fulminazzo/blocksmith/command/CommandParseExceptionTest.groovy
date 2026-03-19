package it.fulminazzo.blocksmith.command

import it.fulminazzo.blocksmith.command.annotation.Command
import spock.lang.Specification

class CommandParseExceptionTest extends Specification {

    def 'test that of correctly formats arguments'() {
        given:
        def expected = 'Invalid method sum(int, double): missing Command annotation'

        when:
        def exception = CommandParseException.of('Invalid method %s: missing %s annotation',
                CommandParseExceptionTest.getMethod('sum', int, double), Command
        )

        then:
        exception.message == expected
    }

    static void sum(int a, double b) {

    }

}
