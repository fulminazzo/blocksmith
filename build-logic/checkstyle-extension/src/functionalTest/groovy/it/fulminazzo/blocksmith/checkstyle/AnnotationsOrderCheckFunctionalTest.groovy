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
        violations.size() == 4

        and:
        def equalsAndHashCode = violations[0]
        equalsAndHashCode.line == 8
        equalsAndHashCode.column == 1
        equalsAndHashCode.message == expectedMessage

        and:
        def builder = violations[1]
        builder.line == 10
        builder.column == 1
        builder.message == expectedMessage

        and:
        def getter = violations[2]
        getter.line == 11
        getter.column == 1
        getter.message == expectedMessage

        and:
        def setter = violations[3]
        setter.line == 12
        setter.column == 1
        setter.message == expectedMessage
    }

}
