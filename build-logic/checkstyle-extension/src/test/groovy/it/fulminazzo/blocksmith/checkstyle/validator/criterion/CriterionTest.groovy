package it.fulminazzo.blocksmith.checkstyle.validator.criterion

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import org.mockito.Mockito
import spock.lang.Specification

class CriterionTest extends Specification {

    def 'test that #criterion returns #expected for #modifier present=#present and #message'() {
        given:
        def node = Mock(DetailAST)

        and:
        def mock = Mockito.mockStatic(CriterionUtils)
        mock.when { CriterionUtils.isModifierPresent(node, TokenTypes."$modifier") }.thenReturn(present)

        expect:
        criterion.matches(node) == expected

        and:
        criterion.errorMessage == message

        cleanup:
        mock.close()

        where:
        criterion                          | modifier            | present || expected | message
        // FieldMutability
        FieldMutabilityCriterion.FINAL     | 'FINAL'             | true    || true     | 'field.final'
        FieldMutabilityCriterion.FINAL     | 'FINAL'             | false   || false    | 'field.final'
        FieldMutabilityCriterion.NON_FINAL | 'FINAL'             | true    || false    | 'field.non-final'
        FieldMutabilityCriterion.NON_FINAL | 'FINAL'             | false   || true     | 'field.non-final'
        // Static
        StaticCriterion.STATIC             | 'LITERAL_STATIC'    | true    || true     | 'static'
        StaticCriterion.STATIC             | 'LITERAL_STATIC'    | false   || false    | 'static'
        StaticCriterion.NON_STATIC         | 'LITERAL_STATIC'    | true    || false    | 'non-static'
        StaticCriterion.NON_STATIC         | 'LITERAL_STATIC'    | false   || true     | 'non-static'
        // Visibility
        VisibilityCriterion.PUBLIC         | 'LITERAL_PUBLIC'    | true    || true     | 'public'
        VisibilityCriterion.PUBLIC         | 'LITERAL_PUBLIC'    | false   || false    | 'public'
        VisibilityCriterion.PROTECTED      | 'LITERAL_PROTECTED' | true    || true     | 'protected'
        VisibilityCriterion.PROTECTED      | 'LITERAL_PROTECTED' | false   || false    | 'protected'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PUBLIC'    | true    || false    | 'package'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PUBLIC'    | false   || true     | 'package'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PROTECTED' | true    || false    | 'package'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PROTECTED' | false   || true     | 'package'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PRIVATE'   | true    || false    | 'package'
        VisibilityCriterion.PACKAGE        | 'LITERAL_PRIVATE'   | false   || true     | 'package'
        VisibilityCriterion.PRIVATE        | 'LITERAL_PRIVATE'   | true    || true     | 'private'
        VisibilityCriterion.PRIVATE        | 'LITERAL_PRIVATE'   | false   || false    | 'private'
    }

}
