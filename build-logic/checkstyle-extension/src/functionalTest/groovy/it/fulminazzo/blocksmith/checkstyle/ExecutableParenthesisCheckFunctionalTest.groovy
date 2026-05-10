package it.fulminazzo.blocksmith.checkstyle

import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message

class ExecutableParenthesisCheckFunctionalTest extends Specification {

    def 'test that invalid executables throw'() {
        given:
        final noParametersMessage = message('parenthesis.noParameters')
        final parametersMessage = message('parenthesis.parameters')

        when:
        def violations = runCheck('ExecutableInvalidParenthesis', ExecutableParenthesisCheck)

        then:
        violations.size() == 5

        and:
        def v1 = violations[0]
        v1.line == 7
        v1.column == 19
        v1.message == parametersMessage

        and:
        def v2 = violations[1]
        v2.line == 8
        v2.column == 13
        v2.message == parametersMessage

        and:
        def v3 = violations[2]
        v3.line == 12
        v3.column == 34
        v3.message == parametersMessage

        and:
        def v4 = violations[3]
        v4.line == 13
        v4.column == 34
        v4.message == parametersMessage

        and:
        def v5 = violations[4]
        v5.line == 17
        v5.column == 5
        v5.message == noParametersMessage
    }

}
