package it.fulminazzo.blocksmith.application

import spock.lang.Specification

class InitializationContextTest extends Specification {
    private static final SimpleObject SIMPLE = new SimpleObject('Alex')
    private static final NestedObject NESTED = new NestedObject(
            'Steve',
            SIMPLE,
            ['name' : 'Rogers']
    )
    private static final Map<String, Object> MAP = ['object' : NESTED]

    def 'test that get of #name in #environment returns #expected'() {
        given:
        def context = new InitializationContext(Mock(Application), environment)

        when:
        def actual = context[name]

        then:
        actual == expected

        where:
        expected || name                     | environment
        // Simple
        'Alex'   || 'name'                   | ['name' : 'Alex']
        // Simple Nested
        SIMPLE   || 'object'                 | ['object' : SIMPLE]
        'Alex'   || 'object.name'            | ['object' : SIMPLE]
        // Complex nested
        NESTED   || 'object'                 | MAP
        'Steve'  || 'object.name'            | MAP
        'Alex'   || 'object.simple.name'     | MAP
        'Rogers' || 'object.map.name'        | MAP
        // Complex nested map
        MAP      || 'map'                    | ['map' : MAP]
        NESTED   || 'map.object'             | ['map' : MAP]
        'Steve'  || 'map.object.name'        | ['map' : MAP]
        'Alex'   || 'map.object.simple.name' | ['map' : MAP]
        'Rogers' || 'map.object.map.name'    | ['map' : MAP]
    }

    def 'test that get of #name in #environment throws'() {
        given:
        def context = new InitializationContext(Mock(Application), environment)

        when:
        context[name]

        then:
        thrown(ApplicationInitializeException)

        where:
        name                            | environment
        'object.lastname.nested'        | MAP
        'object.simple.lastname.nested' | MAP
        'object.map.lastname.nested'    | MAP
    }

}
