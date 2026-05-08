package it.fulminazzo.blocksmith.checker

import it.fulminazzo.blocksmith.checker.validator.MutabilityValidator
import it.fulminazzo.blocksmith.checker.validator.StaticValidator
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checker.FunctionalTestUtils.*

class FieldsOrderCheckFunctionalTest extends Specification {

    def 'test that valid static ordering does not throw'() {
        when:
        def violations = runCheck('ValidStatic', FieldsOrderCheck)

        then:
        violations.empty
    }

    def 'test that static after non-static throws'() {
        when:
        def violations = runCheck('InvalidStatic', FieldsOrderCheck)

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(StaticValidator.STATIC.errorMessage)
    }

    def 'test that valid final ordering does not throw'() {
        when:
        def violations = runCheck('ValidFinal', FieldsOrderCheck)

        then:
        violations.empty
    }

    def 'test that final after non-final throws'() {
        when:
        def violations = runCheck('InvalidFinal', FieldsOrderCheck)

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(MutabilityValidator.FINAL.errorMessage)
    }

    def 'test that valid #modifier ordering does not throw'() {
        when:
        def violations = runCheck('ValidVisibilityModifier', FieldsOrderCheck) {
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

    def 'test that #modifier after #other throws'() {
        when:
        def violations = runCheck('InvalidVisibilityModifier', FieldsOrderCheck) {
            it.replace('%target%', modifier).replace('%other%', other)
        }

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(VisibilityValidator.valueOf(
                modifier.empty ? VisibilityValidator.PACKAGE.name() : modifier.toUpperCase().trim()
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
        def violations = runCheck('InvalidFieldInNestedClass', FieldsOrderCheck)

        then:
        violations.size() == 2

        and:
        def nestedViolation = violations[0]
        nestedViolation.line == 14
        nestedViolation.column == 9
        nestedViolation.message == message(VisibilityValidator.PUBLIC.errorMessage)

        and:
        def violation = violations[1]
        violation.line == 18
        violation.column == 5
        violation.message == message(VisibilityValidator.PACKAGE.errorMessage)
    }

}