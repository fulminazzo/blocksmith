package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import it.fulminazzo.blocksmith.checker.validator.MutabilityValidator
import it.fulminazzo.blocksmith.checker.validator.StaticValidator
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator
import spock.lang.Specification

class NodeValidatorTest extends Specification {

    def 'test that computeScore of node with #modifiers returns #expected'() {
        given:
        def validator = new NodeValidator(
                StaticValidator.values(),
                MutabilityValidator.values(),
                VisibilityValidator.values()
        )

        and:
        def node = Mock(DetailAST)
        for (def modifier : modifiers)
            node.findFirstToken(TokenTypes."$modifier") >> Mock(DetailAST)

        when:
        def score = validator.computeScore(node)

        then:
        score == expected

        where:
        modifiers                                        || expected
        ['LITERAL_STATIC', 'FINAL', 'LITERAL_PUBLIC']    || 0
        ['LITERAL_STATIC', 'FINAL', 'LITERAL_PROTECTED'] || 1
        ['LITERAL_STATIC', 'FINAL']                      || 2
        ['LITERAL_STATIC', 'FINAL', 'LITERAL_PRIVATE']   || 3
        ['LITERAL_STATIC', 'LITERAL_PUBLIC']             || 4
        ['LITERAL_STATIC', 'LITERAL_PROTECTED']          || 5
        ['LITERAL_STATIC',]                              || 6
        ['LITERAL_STATIC', 'LITERAL_PRIVATE']            || 7
        ['FINAL', 'LITERAL_PUBLIC']                      || 8
        ['FINAL', 'LITERAL_PROTECTED']                   || 9
        ['FINAL']                                        || 10
        ['FINAL', 'LITERAL_PRIVATE']                     || 11
        ['LITERAL_PUBLIC']                               || 12
        ['LITERAL_PROTECTED']                            || 13
        []                                               || 14
        ['LITERAL_PRIVATE']                              || 15
    }

}
