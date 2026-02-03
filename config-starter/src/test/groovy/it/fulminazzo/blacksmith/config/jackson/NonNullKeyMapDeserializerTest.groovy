package it.fulminazzo.blacksmith.config.jackson

import spock.lang.Specification

class NonNullKeyMapDeserializerTest extends Specification {

    def 'test that cleanupMap of #map removes null keys'() {
        given:
        def expected = [
                'name': 'Alex',
                'lastname': 'Fulminazzo'
        ]

        when:
        def actual = NonNullKeyMapDeserializer.cleanupMap(map)

        then:
        actual == expected

        where:
        map << [
                ['name': 'Alex', 'lastname': 'Fulminazzo'],
                ['name': 'Alex', 'lastname': 'Fulminazzo', (null): 10],
                ['name': 'Alex', 'lastname': 'Fulminazzo', (null): 10].asImmutable()
        ]
    }

}
