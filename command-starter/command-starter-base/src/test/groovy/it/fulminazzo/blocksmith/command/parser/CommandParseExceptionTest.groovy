//file:noinspection unused
package it.fulminazzo.blocksmith.command.parser

import it.fulminazzo.blocksmith.command.annotation.Command
import spock.lang.Specification

class CommandParseExceptionTest extends Specification {

    def 'test that of correctly formats arguments'() {
        given:
        def expected = "Invalid method void sum(int, double): missing Command annotation. Check $CommandToken.LITERAL.token for more"

        when:
        def exception = CommandParseException.of('Invalid method %s: missing %s annotation. Check %s for more',
                CommandParseExceptionTest.getMethod('sum', int, double), Command, CommandToken.LITERAL
        )

        then:
        exception.message == expected
    }

    static void sum(int a, double b) {

    }

}
