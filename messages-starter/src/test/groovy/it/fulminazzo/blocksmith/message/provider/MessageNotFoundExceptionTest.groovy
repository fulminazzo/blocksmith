package it.fulminazzo.blocksmith.message.provider

import spock.lang.Specification

class MessageNotFoundExceptionTest extends Specification {

    def 'test that exception returns correct message'() {
        given:
        def exception = new MessageNotFoundException('path', Locale.ITALY)

        expect:
        exception.message == 'Could not find message with path \'path\' and locale \'it-it\''
    }

}
