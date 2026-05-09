package it.fulminazzo.blocksmith.checkstyle

import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck

class ConstructorsOrderCheckFunctionalTest extends Specification {

    def 'test that valid visibility #modifier ordering does not throw'() {
        when:
        def violations = runCheck('ConstructorValidVisibilityModifier', ConstructorsOrderCheck) {
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
        def violations = runCheck('ConstructorInvalidVisibilityModifier', ConstructorsOrderCheck) {
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

    def 'test that valid overloads ordering does not throw'() {
        when:
        def violations = runCheck('ConstructorValidOverload', ConstructorsOrderCheck)

        then:
        violations.empty
    }

    def 'test that invalid overloads throws'() {
        given:
        final msg = 'it.fulminazzo.blocksmith.checkstyle.overload'

        when:
        def violations = runCheck('ConstructorInvalidOverload', ConstructorsOrderCheck)

        then:
        violations.size() == 4

        and:
        def v1 = violations[0]
        v1.line == 9
        v1.column == 5
        v1.message == message(msg)

        and:
        def v2 = violations[1]
        v2.line == 15
        v2.column == 5
        v2.message == message(msg)

        and:
        def v3 = violations[2]
        v3.line == 17
        v3.column == 5
        v3.message == message(msg)

        and:
        def v4 = violations[3]
        v4.line == 19
        v4.column == 5
        v4.message == message(msg)
    }

    def 'test that constructors check works on nested classes'() {
        when:
        def violations = runCheck('ConstructorInvalidInNestedClass', ConstructorsOrderCheck)

        then:
        violations.size() == 2

        and:
        def nestedViolation = violations[0]
        nestedViolation.line == 15
        nestedViolation.column == 9
        nestedViolation.message == message('it.fulminazzo.blocksmith.checkstyle.overload')

        and:
        def violation = violations[1]
        violation.line == 19
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.overload')
    }

}
