package it.fulminazzo.blocksmith.checker

import com.puppycrawl.tools.checkstyle.api.DetailAST
import spock.lang.Specification

class RankerImplTest extends Specification {

    def 'test that computeScore throws if it could not compute score'() {
        given:
        def ranker = new RankerImpl()

        when:
        ranker.computeScore(Mock(DetailAST))

        then:
        thrown(IllegalArgumentException)
    }

}
