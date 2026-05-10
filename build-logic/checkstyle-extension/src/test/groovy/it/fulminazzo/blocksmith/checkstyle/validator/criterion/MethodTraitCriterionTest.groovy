package it.fulminazzo.blocksmith.checkstyle.validator.criterion

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import org.mockito.Mockito
import spock.lang.Specification

class MethodTraitCriterionTest extends Specification {

    def 'test that #criterion returns #expected for #modifier present=#present and #annotationPresent and #message'() {
        given:
        def node = Mock(DetailAST)

        and:
        def mock = Mockito.mockStatic(CriterionUtils)
        mock.when { CriterionUtils.isModifierPresent(node, TokenTypes."$modifier") }.thenReturn(present)
        mock.when { CriterionUtils.isAnnotatedWith(node, 'Override') }.thenReturn(annotationPresent)

        expect:
        criterion.matches(node) == expected

        and:
        criterion.errorMessage == message

        cleanup:
        mock.close()

        where:
        criterion                     | modifier         | present | annotationPresent || expected | message
        // ABSTRACT
        MethodTraitCriterion.ABSTRACT | 'ABSTRACT'       | true    | true              || true     | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'ABSTRACT'       | true    | false             || true     | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'ABSTRACT'       | false   | true              || false    | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'ABSTRACT'       | false   | false             || false    | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'LITERAL_STATIC' | true    | true              || false    | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'LITERAL_STATIC' | true    | false             || false    | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'LITERAL_STATIC' | false   | true              || false    | 'method.abstract'
        MethodTraitCriterion.ABSTRACT | 'LITERAL_STATIC' | false   | false             || false    | 'method.abstract'
        // CONCRETE
        MethodTraitCriterion.CONCRETE | 'ABSTRACT'       | true    | true              || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'ABSTRACT'       | true    | false             || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'ABSTRACT'       | false   | true              || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'ABSTRACT'       | false   | false             || true     | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'LITERAL_STATIC' | true    | true              || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'LITERAL_STATIC' | true    | false             || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'LITERAL_STATIC' | false   | true              || false    | 'method.concrete'
        MethodTraitCriterion.CONCRETE | 'LITERAL_STATIC' | false   | false             || true     | 'method.concrete'
        // OVERRIDE
        MethodTraitCriterion.OVERRIDE | 'ABSTRACT'       | true    | true              || true     | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'ABSTRACT'       | true    | false             || false    | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'ABSTRACT'       | false   | true              || true     | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'ABSTRACT'       | false   | false             || false    | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'LITERAL_STATIC' | true    | true              || true     | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'LITERAL_STATIC' | true    | false             || false    | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'LITERAL_STATIC' | false   | true              || true     | 'method.override'
        MethodTraitCriterion.OVERRIDE | 'LITERAL_STATIC' | false   | false             || false    | 'method.override'
        // STATIC
        MethodTraitCriterion.STATIC   | 'ABSTRACT'       | true    | true              || false    | 'method.static'
        MethodTraitCriterion.STATIC   | 'ABSTRACT'       | true    | false             || false    | 'method.static'
        MethodTraitCriterion.STATIC   | 'ABSTRACT'       | false   | true              || false    | 'method.static'
        MethodTraitCriterion.STATIC   | 'ABSTRACT'       | false   | false             || false    | 'method.static'
        MethodTraitCriterion.STATIC   | 'LITERAL_STATIC' | true    | true              || true     | 'method.static'
        MethodTraitCriterion.STATIC   | 'LITERAL_STATIC' | true    | false             || true     | 'method.static'
        MethodTraitCriterion.STATIC   | 'LITERAL_STATIC' | false   | true              || false    | 'method.static'
        MethodTraitCriterion.STATIC   | 'LITERAL_STATIC' | false   | false             || false    | 'method.static'
    }

}
