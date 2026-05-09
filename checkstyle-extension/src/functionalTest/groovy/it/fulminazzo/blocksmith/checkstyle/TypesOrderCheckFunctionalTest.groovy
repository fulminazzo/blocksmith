package it.fulminazzo.blocksmith.checkstyle

import it.fulminazzo.blocksmith.checkstyle.validator.criterion.TypeCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck

class TypesOrderCheckFunctionalTest extends Specification {

    def 'test that valid type #modifier ordering does not throw'() {
        when:
        def violations = runCheck('TypeValidType', TypesOrderCheck) {
            it = it.replace('%target%', modifier).replace('%other%', other)
            applyRecordSyntax(it)
        }

        then:
        violations.empty

        where:
        modifier    | other
        // interface
        'interface' | 'interface'
        'interface' | 'enum'
        'interface' | 'record'
        'interface' | 'class'
        // enum
        'enum'      | 'enum'
        'enum'      | 'record'
        'enum'      | 'class'
        // record
        'record'    | 'record'
        'record'    | 'class'
        // class
        'class'     | 'class'
    }

    def 'test that type #modifier after #other throws'() {
        when:
        def violations = runCheck('TypeInvalidType', TypesOrderCheck) {
            it = it.replace('%target%', modifier).replace('%other%', other)
            applyRecordSyntax(it)
        }

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(TypeCriterion.valueOf(modifier.toUpperCase().trim()).errorMessage)

        where:
        modifier    | other
        // interface
        'interface' | 'enum'
        'interface' | 'record'
        'interface' | 'class'
        // enum
        'enum'      | 'record'
        'enum'      | 'class'
        // record
        'record'    | 'class'
    }

    def 'test that valid static ordering does not throw'() {
        when:
        def violations = runCheck('TypeValidStatic', TypesOrderCheck)

        then:
        violations.empty
    }

    def 'test that static after non-static throws'() {
        when:
        def violations = runCheck('TypeInvalidStatic', TypesOrderCheck)

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.static')
    }

    def 'test that valid visibility #modifier ordering does not throw'() {
        when:
        def violations = runCheck('TypeValidVisibilityModifier', TypesOrderCheck) {
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
        def violations = runCheck('TypeInvalidVisibilityModifier', TypesOrderCheck) {
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

    def 'test that types check works on nested classes'() {
        when:
        def violations = runCheck('TypeInvalidInNestedClass', TypesOrderCheck)

        then:
        violations.size() == 3

        and:
        def nestViolation = violations[0]
        nestViolation.line == 9
        nestViolation.column == 5
        nestViolation.message == message('it.fulminazzo.blocksmith.checkstyle.public')

        and:
        def nestedViolation = violations[1]
        nestedViolation.line == 14
        nestedViolation.column == 9
        nestedViolation.message == message('it.fulminazzo.blocksmith.checkstyle.public')

        and:
        def violation = violations[2]
        violation.line == 18
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.package')
    }

    private static String applyRecordSyntax(final String line) {
        def matcher = line =~ /(.* record [A-Za-z\d_]+)( \{.*)/
        if (matcher) {
            def match = matcher[0]
            return "${match[1]}()${match[2]}"
        }
        return line
    }

}
