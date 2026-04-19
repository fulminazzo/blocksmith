package it.fulminazzo.blocksmith.naming

import spock.lang.Specification

class CamelCaseConventionTest extends Specification {

    def 'test that tokenize of #tokens returns #expected'() {
        given:
        def convention = new CamelCaseConvention()

        when:
        def actual = convention.format(tokens)

        then:
        actual == expected

        where:
        tokens   || expected
        []       || ''
        ['', ''] || ''
    }

}
