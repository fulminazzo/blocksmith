package it.fulminazzo.blocksmith.checkstyle.validator.criterion

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import spock.lang.Specification

class CriterionUtilsTest extends Specification {

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

    def 'test that isModifierPresent of no modifiers returns false'() {
        given:
        def node = Mock(DetailAST)

        expect:
        !CriterionUtils.isModifierPresent(node, TokenTypes.LITERAL_PUBLIC)
    }

}
