package it.fulminazzo.config.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import spock.lang.Specification

class LoggerDeserializationProblemHandlerTest extends Specification {
    private Logger logger
    private ObjectMapper mapper

    void setup() {
        logger = Mock()
        mapper = JacksonUtils.setupMapper(new ObjectMapper(), logger)
    }

    def 'test that handleUnknownProperty logs correctly'() {
        given:
        def json = mapper.writeValueAsString([
                'person1': [
                        'name'    : 'Alex',
                        'lastname': 'Fulminazzo',
                        'age'     : 23,
                        'street'  : 'Duomo square'
                ],
                'person2': new Person('Camilla', 'Drinkwater', 20)
        ])

        and:
        def typeFactory = mapper.typeFactory
        def mapType = typeFactory.constructMapType(Map, String, Person)

        when:
        def value = mapper.readValue(json, mapType)

        then:
        noExceptionThrown()

        and:
        value == [
                'person1': new Person('Alex', 'Fulminazzo', 23),
                'person2': new Person('Camilla', 'Drinkwater', 20)
        ]

        and:
        1 * logger.warn('Ignoring unrecognized property \'{}\' (path: \'{}\')', 'street', 'person1.street')
    }

    def 'test that handleWeirdKey logs correctly and returns a map without the marker'() {
        given:
        def json = mapper.writeValueAsString([
                1        : 'Alex',
                2        : 'Fulminazzo',
                'invalid': 'invisible'
        ])

        and:
        def typeFactory = mapper.typeFactory
        def mapType = typeFactory.constructMapType(Map, Integer, String)

        when:
        def value = mapper.readValue(json, mapType)

        then:
        noExceptionThrown()

        and:
        value == [
                1: 'Alex',
                2: 'Fulminazzo'
        ]

        and:
        1 * logger.warn('Invalid key \'{}\' for map: expected {} (path: \'{}\')', 'invalid', Integer.canonicalName, 'invalid')
    }

}
