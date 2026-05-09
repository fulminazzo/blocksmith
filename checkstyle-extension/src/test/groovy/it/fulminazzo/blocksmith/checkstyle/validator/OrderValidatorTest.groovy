package it.fulminazzo.blocksmith.checkstyle.validator

import com.puppycrawl.tools.checkstyle.api.DetailAST
import com.puppycrawl.tools.checkstyle.api.TokenTypes
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.MutabilityCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.StaticCriterion
import it.fulminazzo.blocksmith.checkstyle.validator.criterion.VisibilityCriterion
import spock.lang.Specification

class OrderValidatorTest extends Specification {
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

    private final OrderValidator validator = new OrderValidator(
            StaticCriterion.values(),
            MutabilityCriterion.values(),
            VisibilityCriterion.values()
    )

    def 'test that validateNode when lastScore is #lastScore allows node with #modifiers and updates to #expected'() {
        given:
        def node = generateNode(modifiers)

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
        def node = generateNode(modifiers)

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

    def 'test that exitScope throws if ordered nodes are incorrect'() {
        given:
        def first = generateNode(['LITERAL_STATIC'])
        def second = generateNode(['LITERAL_STATIC', 'FINAL'])

        and:
        def validator = new OrderValidator(StaticCriterion.values())
                .then(new OrderValidator(MutabilityCriterion.values()))

        when:
        validator.validateNode(first)
        validator.validateNode(second)

        then:
        noExceptionThrown()

        when:
        validator.exitScope()

        then:
        def e = thrown(ValidationException)
        e.node == second
        e.message == 'it.fulminazzo.blocksmith.checkstyle.order.final'
    }

    def 'test that computeScore of node with #modifiers returns #expected'() {
        given:
        def node = generateNode(modifiers)

        when:
        def score = validator.computeScore(node)

        then:
        score == expected

        where:
        [modifiers, expected] << ENCODED_MODIFIERS.collect { [it.value, it.key] }
    }

    def 'test that getMaxScore returns correct value'() {
        expect:
        validator.maxScore == 15
    }

    private DetailAST generateNode(final List<String> modifiers) {
        def node = Mock(DetailAST)

        def modifiersNodes = modifiers.collect {
            def n = Mock(DetailAST)
            n.type >> TokenTypes."$it"
            return n
        }
        def size = modifiersNodes.size()
        if (size > 1) {
            for (i in 0..size - 2)
                modifiersNodes[i].nextSibling >> modifiersNodes[i + 1]
        }

        def modifiersNode = Mock(DetailAST)
        modifiersNode.firstChild >> modifiersNodes[0]
        node.findFirstToken(TokenTypes.MODIFIERS) >> modifiersNode

        return node
    }

}
