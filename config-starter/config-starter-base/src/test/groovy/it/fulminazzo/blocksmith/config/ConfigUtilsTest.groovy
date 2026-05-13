package it.fulminazzo.blocksmith.config

import it.fulminazzo.blocksmith.naming.Convention
import spock.lang.Specification

class ConfigUtilsTest extends Specification {
    private static final Object MOCK_CONFIGURATION = new Object()

    def 'test that mergeDataMaps works'() {
        given:
        def configuration = [
                'simple'        : true,
                'primitive'     : '1.0',
                'nested'        : ['hello' : 'world'],
                'absent-nested' : ['goodbye' : 'mars']
        ]

        and:
        def comments = [
                'simple'    : ['A simple configuration'],
                'primitive' : 'This should be ignored',
                'nested'    : ['hello' : ['The greeting']]
        ]

        and:
        def expected = [
                (new CommentKey('simple', ['A simple configuration'])) : true,
                (new CommentKey('primitive'))                          : 1.0,
                (new CommentKey('nested'))                             : [
                        (new CommentKey('hello', ['The greeting'])) : 'world'
                ],
                (new CommentKey('absent-nested'))                      : [
                        (new CommentKey('goodbye')) : 'mars'
                ]
        ]

        when:
        def map = ConfigUtils.mergeDataMaps(configuration, comments)

        then:
        map == expected
    }

    def 'test that checkMap of #configuration returns #expected'() {
        when:
        def actual = ConfigUtils.checkMap(configuration, Convention.KEBAB_CASE, Convention.CAMEL_CASE)

        then:
        actual == expected

        where:
        configuration                                                                || expected
        MOCK_CONFIGURATION                                                           || MOCK_CONFIGURATION
        ['first-value' : 1]                                                          || ['firstValue' : 1]
        ['first-value' : 1, 'second-value' : true]                                   || ['firstValue' : 1, 'secondValue' : true]
        [
                'first-value'  : 1,
                'second-value' : true,
                'nested-value' : ['information' : 'absent', 'player-online' : false]
        ]                                                                            || [
                'firstValue'  : 1,
                'secondValue' : true,
                'nestedValue' : ['information' : 'absent', 'playerOnline' : false]
        ]
    }

}
