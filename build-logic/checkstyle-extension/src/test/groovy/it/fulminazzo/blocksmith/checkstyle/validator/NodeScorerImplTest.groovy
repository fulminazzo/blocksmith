package it.fulminazzo.blocksmith.checkstyle.validator

import com.puppycrawl.tools.checkstyle.api.DetailAST
import spock.lang.Specification

class NodeScorerImplTest extends Specification {

    def 'test that computeScore throws if it could not compute score'() {
        given:
        def scorer = new NodeScorerImpl()

        when:
        scorer.computeScore(Mock(DetailAST))

        then:
        thrown(IllegalArgumentException)
    }

}
