package it.fulminazzo.blocksmith.checkstyle

import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck

class DeclarationOrderCheckFunctionalTest extends Specification {

    def 'test that invalid order throws'() {
        given:
        final expectedMessage = message('it.fulminazzo.blocksmith.checkstyle.declaration')

        when:
        def violations = runCheck('DeclarationInvalidOrder', DeclarationOrderCheck)

        then:
        violations.size() == 3

        and:
        def firstMethod = violations[0]
        firstMethod.line == 9
        firstMethod.column == 5
        firstMethod.message == expectedMessage

        and:
        def secondMethod = violations[1]
        secondMethod.line == 14
        secondMethod.column == 5
        secondMethod.message == expectedMessage

        and:
        def constructor = violations[2]
        constructor.line == 16
        constructor.column == 5
        constructor.message == expectedMessage
    }

}
