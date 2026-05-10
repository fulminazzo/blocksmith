package it.fulminazzo.blocksmith.checkstyle

import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MethodNameCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MethodTraitCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion
import spock.lang.Specification

import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.message
import static it.fulminazzo.blocksmith.checkstyle.FunctionalTestUtils.runCheck

class MethodsOrderCheckFunctionalTest extends Specification {

    def 'test that valid method trait #methodTrait ordering does not throw'() {
        when:
        def violations = runCheck('MethodValidVisibilityModifier', MethodsOrderCheck) {
            it.replace('%target%', methodTrait).replace('%other%', other)
        }

        then:
        violations.empty

        where:
        methodTrait   | other
        // abstract
        'abstract '   | 'abstract '
        'abstract '   | ''
        'abstract '   | '@Override\n'
        'abstract '   | 'static '
        // concrete
        ''            | ''
        ''            | '@Override\n'
        ''            | 'static '
        // override
        '@Override\n' | '@Override\n'
        '@Override\n' | 'static '
        // static
        'static '     | 'static '
    }

    def 'test that method trait #methodTrait after #other throws'() {
        when:
        def violations = runCheck('MethodInvalidVisibilityModifier', MethodsOrderCheck) {
            it.replace('%target%', methodTrait).replace('%other%', other)
        }

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(msg)

        where:
        methodTrait  | other        || msg
        // abstract
        'abstract '  | ''           || MethodTraitCriterion.ABSTRACT.errorMessage
        'abstract '  | '@Override ' || MethodTraitCriterion.ABSTRACT.errorMessage
        'abstract '  | 'static '    || MethodTraitCriterion.ABSTRACT.errorMessage
        // concrete
        ''           | '@Override ' || MethodTraitCriterion.CONCRETE.errorMessage
        ''           | 'static '    || MethodTraitCriterion.CONCRETE.errorMessage
        // override
        '@Override ' | 'static '    || MethodTraitCriterion.OVERRIDE.errorMessage
    }

    def 'test that valid visibility #modifier ordering does not throw'() {
        when:
        def violations = runCheck('MethodValidVisibilityModifier', MethodsOrderCheck) {
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
        def violations = runCheck('MethodInvalidVisibilityModifier', MethodsOrderCheck) {
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
        def violations = runCheck('MethodValidOverload', MethodsOrderCheck)

        then:
        violations.empty
    }

    def 'test that invalid overloads throws'() {
        given:
        final msg = 'it.fulminazzo.blocksmith.checkstyle.executable.overload'

        when:
        def violations = runCheck('MethodInvalidOverload', MethodsOrderCheck)

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

    def 'test that valid overload grouping does not throw'() {
        when:
        def violations = runCheck('MethodValidOverloadGrouping', MethodsOrderCheck)

        then:
        violations.empty
    }

    def 'test that invalid overload grouping throws'() {
        given:
        final msg = 'it.fulminazzo.blocksmith.checkstyle.executable.overload.grouped'

        when:
        def violations = runCheck('MethodInvalidOverloadGrouping', MethodsOrderCheck)

        then:
        violations.size() == 2

        and:
        def v1 = violations[0]
        v1.line == 9
        v1.column == 5
        v1.message == message(msg)

        and:
        def v2 = violations[1]
        v2.line == 13
        v2.column == 5
        v2.message == message(msg)
    }

    def 'test that valid method name #name ordering does not throw'() {
        when:
        def violations = runCheck('MethodValidName', MethodsOrderCheck) {
            it.replace('%target%', name).replace('%other%', other)
        }

        then:
        violations.empty

        where:
        name        | other
        // any
        'method'    | 'method'
        'method'    | 'getString'
        'method'    | 'setString'
        'method'    | 'equals'
        'method'    | 'hashCode'
        'method'    | 'toString'
        // getter and setter
        'getString' | 'getString'
        'getString' | 'setString'
        'getString' | 'equals'
        'getString' | 'hashCode'
        'getString' | 'toString'
        'setString' | 'getString'
        'setString' | 'setString'
        'setString' | 'equals'
        'setString' | 'hashCode'
        'setString' | 'toString'
        // equals
        'equals'    | 'equals'
        'equals'    | 'hashCode'
        'equals'    | 'toString'
        // hashCode
        'hashCode'  | 'hashCode'
        'hashCode'  | 'toString'
        // toString
        'toString'  | 'toString'
    }

    def 'test that method name #name after #other throws'() {
        when:
        def violations = runCheck('MethodInvalidName', MethodsOrderCheck) {
            it.replace('%target%', name).replace('%other%', other)
        }

        then:
        violations.size() == 1

        and:
        def violation = violations[0]
        violation.line == 8
        violation.column == 5
        violation.message == message(msg)

        where:
        name        | other       || msg
        // any
        'method'    | 'getString' || MethodNameCriterion.ANY.errorMessage
        'method'    | 'setString' || MethodNameCriterion.ANY.errorMessage
        'method'    | 'equals'    || MethodNameCriterion.ANY.errorMessage
        'method'    | 'hashCode'  || MethodNameCriterion.ANY.errorMessage
        'method'    | 'toString'  || MethodNameCriterion.ANY.errorMessage
        // getter and setter
        'getString' | 'equals'    || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        'getString' | 'hashCode'  || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        'getString' | 'toString'  || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        'setString' | 'equals'    || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        'setString' | 'hashCode'  || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        'setString' | 'toString'  || MethodNameCriterion.GETTER_AND_SETTER.errorMessage
        // equals
        'equals'    | 'hashCode'  || MethodNameCriterion.EQUALS.errorMessage
        'equals'    | 'toString'  || MethodNameCriterion.EQUALS.errorMessage
        // hashCode
        'hashCode'  | 'toString'  || MethodNameCriterion.HASH_CODE.errorMessage
    }

    def 'test that valid getter-setter ordering does not throw'() {
        when:
        def violations = runCheck('MethodValidGetterSetter', MethodsOrderCheck)

        then:
        violations.empty
    }

    def 'test that invalid getter-setter ordering throws'() {
        when:
        def violations = runCheck('MethodInvalidGetterSetter', MethodsOrderCheck)

        then:
        violations.size() == 2

        and:
        def getterViolation = violations[0]
        getterViolation.line == 9
        getterViolation.column == 5
        getterViolation.message == message('it.fulminazzo.blocksmith.checkstyle.method.pair.first')
                .replace('{0}', 'get').replace('{1}', 'set')

        and:
        def setterViolation = violations[1]
        setterViolation.line == 13
        setterViolation.column == 5
        setterViolation.message == message('it.fulminazzo.blocksmith.checkstyle.method.pair.second')
                .replace('{0}', 'set').replace('{1}', 'is, get')
    }

    def 'test that methods check works on nested classes'() {
        when:
        def violations = runCheck('MethodInvalidInNestedClass', MethodsOrderCheck)

        then:
        violations.size() == 2

        and:
        def nestedViolation = violations[0]
        nestedViolation.line == 15
        nestedViolation.column == 9
        nestedViolation.message == message('it.fulminazzo.blocksmith.checkstyle.executable.overload')

        and:
        def violation = violations[1]
        violation.line == 19
        violation.column == 5
        violation.message == message('it.fulminazzo.blocksmith.checkstyle.executable.overload')
    }

}
