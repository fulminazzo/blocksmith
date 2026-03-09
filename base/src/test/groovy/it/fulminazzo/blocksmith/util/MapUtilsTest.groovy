package it.fulminazzo.blocksmith.util

import net.bytebuddy.agent.builder.AgentBuilder
import spock.lang.Specification

class MapUtilsTest extends Specification {

    def 'test that flattenMap correctly flattens'() {
        given:
        def map = [
                'first': 1.0,
                'second': 'blocksmith',
                'third': new String[]{'Hello', 'world'},
                'fourth': ['Goodbye', 'mars'],
                'fifth': [
                        'type': 'SQL',
                        'port': 3306,
                        'ssl': true,
                        (null): false
                ],
                'sixth': new Person(),
                'seventh': AgentBuilder.PoolStrategy.ClassLoading.EXTENDED,
                'eighth': null
        ]

        and:
        def expected = [
                'first': 1.0,
                'second': 'blocksmith',
                'third[0]': 'Hello',
                'third[1]': 'world',
                'fourth[0]': 'Goodbye',
                'fourth[1]': 'mars',
                'fifth.type': 'SQL',
                'fifth.port': 3306,
                'fifth.ssl': true,
                'fifth.null': false,
                'sixth.name': 'Alex',
                'sixth.age': 23,
                'seventh': AgentBuilder.PoolStrategy.ClassLoading.EXTENDED,
                'eighth': null
        ]

        when:
        def actual = MapUtils.flattenMap(map)

        then:
        actual == expected
    }

    static class Person {

        String name = 'Alex'

        int age = 23

    }

}
