package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import it.fulminazzo.blocksmith.checker.validator.MutabilityValidator
import it.fulminazzo.blocksmith.checker.validator.StaticValidator
import it.fulminazzo.blocksmith.checker.validator.VisibilityValidator
import spock.lang.Specification

class NodeValidatorTest extends Specification {
    private static final Map<Integer, List<String>> ENCODED_MODIFIERS = [
            0  : ['LITERAL_STATIC', 'FINAL', 'LITERAL_PUBLIC'],
            1  : ['LITERAL_STATIC', 'FINAL', 'LITERAL_PROTECTED'],
            2  : ['LITERAL_STATIC', 'FINAL'],
            3  : ['LITERAL_STATIC', 'FINAL', 'LITERAL_PRIVATE'],
            4  : ['LITERAL_STATIC', 'LITERAL_PUBLIC'],
            5  : ['LITERAL_STATIC', 'LITERAL_PROTECTED'],
            6  : ['LITERAL_STATIC',],
            7  : ['LITERAL_STATIC', 'LITERAL_PRIVATE'],
            8  : ['FINAL', 'LITERAL_PUBLIC'],
            9  : ['FINAL', 'LITERAL_PROTECTED'],
            10 : ['FINAL'],
            11 : ['FINAL', 'LITERAL_PRIVATE'],
            12 : ['LITERAL_PUBLIC'],
            13 : ['LITERAL_PROTECTED'],
            14 : [],
            15 : ['LITERAL_PRIVATE']
    ]

    private final NodeValidator validator = new NodeValidator(
            StaticValidator.values(),
            MutabilityValidator.values(),
            VisibilityValidator.values()
    )

    def 'test that validateNode when lastScore is #lastScore allows node with #modifiers and updates to #expected'() {
        given:
        def node = Mock(DetailAST)
        for (def modifier : modifiers)
            node.findFirstToken(TokenTypes."$modifier") >> Mock(DetailAST)

        and:
        validator.lastScore = lastScore

        when:
        validator.validateNode(node)

        then:
        noExceptionThrown()

        and:
        validator.lastScore == expected

        where:
        [lastScore, modifiers, expected] << ENCODED_MODIFIERS.keySet().collectMany { score ->
            ENCODED_MODIFIERS.findAll { it.key >= score }
                    .collect { [score, it.value, it.key] }
        }
    }

    def 'test that validateNode when lastScore is #lastScore throws for node with #modifiers'() {
        given:
        def node = Mock(DetailAST)
        for (def modifier : modifiers)
            node.findFirstToken(TokenTypes."$modifier") >> Mock(DetailAST)

        and:
        validator.lastScore = lastScore

        when:
        validator.validateNode(node)

        then:
        thrown(ValidationException)

        where:
        [lastScore, modifiers] << ENCODED_MODIFIERS.keySet().collectMany { score ->
            ENCODED_MODIFIERS.findAll { it.key < score }
                    .collect { [score, it.value] }
        }
    }

    def 'test that computeScore of node with #modifiers returns #expected'() {
        given:
        def node = Mock(DetailAST)
        for (def modifier : modifiers)
            node.findFirstToken(TokenTypes."$modifier") >> Mock(DetailAST)

        when:
        def score = validator.computeScore(node)

        then:
        score == expected

        where:
        [modifiers, expected] << ENCODED_MODIFIERS.collect { [it.value, it.key] }
    }

}
