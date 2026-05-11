package it.fulminazzo.blocksmith.checkstyle.validator.criterion

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import spock.lang.Specification

class CriterionUtilsTest extends Specification {

    def 'test that isAnnotatedWith works'() {
        given:
        def first = Mock(DetailAST)

        def second = Mock(DetailAST)
        second.findFirstToken(TokenTypes.IDENT) >> {
            def annotation = Mock(DetailAST)
            annotation.text >> 'Second'
            return annotation
        }
        first.nextSibling >> second

        def third = Mock(DetailAST)
        third.findFirstToken(TokenTypes.IDENT) >> {
            def annotation = Mock(DetailAST)
            annotation.text >> 'Third'
            return annotation
        }
        second.nextSibling >> third

        and:
        def modifiers = Mock(DetailAST)
        modifiers.findFirstToken(TokenTypes.ANNOTATION) >> first

        and:
        def node = Mock(DetailAST)
        node.findFirstToken(TokenTypes.MODIFIERS) >> modifiers

        expect:
        !CriterionUtils.isAnnotatedWith(node, 'First')
        CriterionUtils.isAnnotatedWith(node, 'Second')
        CriterionUtils.isAnnotatedWith(node, 'Third')
    }

    def 'test that isModifierPresent works'() {
        given:
        def first = Mock(DetailAST)
        first.type >> TokenTypes.LITERAL_STATIC

        def second = Mock(DetailAST)
        second.type >> TokenTypes.FINAL
        first.nextSibling >> second

        def third = Mock(DetailAST)
        third.type >> TokenTypes.LITERAL_PUBLIC
        second.nextSibling >> third

        and:
        def modifiers = Mock(DetailAST)
        modifiers.firstChild >> first

        and:
        def node = Mock(DetailAST)
        node.findFirstToken(TokenTypes.MODIFIERS) >> modifiers

        expect:
        CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_STATIC)
        CriterionUtils.isModifierPresent(node, TokenTypes.FINAL)
        CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_PUBLIC)
        !CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_TRANSIENT)
    }

    def 'test that #method of no modifiers returns false'() {
        given:
        def node = Mock(DetailAST)

        expect:
        !CriterionUtils."$method"(node, *arguments)

        where:
        method              | arguments
        'isAnnotatedWith'   | ['Annotation']
        'isModifierPresent' | [TokenTypes.LITERAL_PUBLIC]
    }

}
