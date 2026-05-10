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
        violations.size() == 2

        and:
        def equalsAndHashCode = violations[0]
        equalsAndHashCode.line == 9
        equalsAndHashCode.column == 5
        equalsAndHashCode.message == expectedMessage

        and:
        def getter = violations[1]
        getter.line == 11
        getter.column == 5
        getter.message == expectedMessage
    }

}
