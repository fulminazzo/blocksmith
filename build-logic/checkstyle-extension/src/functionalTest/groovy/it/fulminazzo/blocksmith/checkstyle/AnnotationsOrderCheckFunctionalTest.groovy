package it.fulminazzo.blocksmith.checkstyle

import it.fulminazzo.blocksmith.checkstyle.validator.criterion.AnnotationCriterion
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck

class AnnotationsOrderCheckFunctionalTest extends Specification {

    def 'test that invalid order throws'() {
        given:
        final expectedMessage = message('it.fulminazzo.blocksmith.checkstyle.annotation')
                .replace('{0}', String.join(', ', AnnotationCriterion.values()*.toString()))

        when:
        def violations = runCheck('AnnotationInvalidOrder', AnnotationsOrderCheck)

        then:
        violations.size() == 3

        and:
        def equalsAndHashCode = violations[0]
        equalsAndHashCode.line == 8
        equalsAndHashCode.column == 1
        equalsAndHashCode.message == expectedMessage

        and:
        def getter = violations[1]
        getter.line == 10
        getter.column == 1
        getter.message == expectedMessage

        and:
        def setter = violations[2]
        setter.line == 11
        setter.column == 1
        setter.message == expectedMessage
    }

}
