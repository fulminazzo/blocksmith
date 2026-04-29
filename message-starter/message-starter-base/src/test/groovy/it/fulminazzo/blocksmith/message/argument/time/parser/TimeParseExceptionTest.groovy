package it.fulminazzo.blocksmith.message.argument.time.parser

import spock.lang.Specification

class TimeParseExceptionTest extends Specification {

    def 'test that of function correctly parses arguments'() {
        when:
        def exception = TimeParseException.of('%s, %s!', 'Hello', TimeToken.TEXT)

        then:
        exception.message == "Hello, ${TimeToken.TEXT.getToken()}!"
    }

}
