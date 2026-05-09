package it.fulminazzo.blocksmith.checkstyle

import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MutabilityCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.StaticCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.*

class FieldsOrderCheckFunctionalTest extends Specification {

    def 'test that valid static ordering does not throw'() {
        when:
        def violations = runCheck('FieldValidStatic', FieldsOrderCheck)

        then:
        violations.empty
    }

    def 'test that static after non-static throws'() {
        when:
        def violations = runCheck('FieldInvalidStatic', FieldsOrderCheck)

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.static')
    }

    def 'test that valid final ordering does not throw'() {
        when:
        def violations = runCheck('FieldValidFinal', FieldsOrderCheck)

        then:
        violations.empty
    }

    def 'test that final after non-final throws'() {
        when:
        def violations = runCheck('FieldInvalidFinal', FieldsOrderCheck)

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.final')
    }

    def 'test that valid visibility #modifier ordering does not throw'() {
        when:
        def violations = runCheck('FieldValidVisibilityModifier', FieldsOrderCheck) {
            it.replace('%target%', modifier).replace('%other%', other)
        }

        then:
        violations.empty

        where:
        modifier     | other
        // public
        'public '    | 'public '
        'public '    | 'protected '
        'public '    | ''
        'public '    | 'private '
        // protected
        'protected ' | 'protected '
        'protected ' | ''
        'protected ' | 'private '
        // package
        ''           | ''
        ''           | 'private '
        // private
        'private '   | 'private '
    }

    def 'test that visibility #modifier after #other throws'() {
        when:
        def violations = runCheck('FieldInvalidVisibilityModifier', FieldsOrderCheck) {
            it.replace('%target%', modifier).replace('%other%', other)
        }

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(VisibilityCriterion.valueOf(
                modifier.empty ? VisibilityCriterion.PACKAGE.name() : modifier.toUpperCase().trim()
        ).errorMessage)

        where:
        modifier     | other
        // public
        'public '    | 'protected '
        'public '    | ''
        'public '    | 'private '
        // protected
        'protected ' | ''
        'protected ' | 'private '
        // package
        ''           | 'private '
    }

    def 'test that fields check works on nested classes'() {
        when:
        def violations = runCheck('FieldInvalidInNestedClass', FieldsOrderCheck)

        then:
        violations.size() == 2

        and:
        def nestedViolation = violations[0]
        nestedViolation.line == 14
        nestedViolation.column == 9
        nestedViolation.message == message('it.fulminazzo.blocksmith.checkstyle.public')

        and:
        def violation = violations[1]
        violation.line == 18
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.package')
    }

}