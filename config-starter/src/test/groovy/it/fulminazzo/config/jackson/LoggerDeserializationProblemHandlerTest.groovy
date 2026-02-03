package it.fulminazzo.config.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import org.slf4j.Logger
import spock.lang.Specification

class LoggerDeserializationProblemHandlerTest extends Specification {
    private Logger logger
    private ObjectMapper mapper

    void setup() {
        logger = Mock()
        mapper = JacksonUtils.setupMapper(new ObjectMapper(), logger)
        .registerModule(new SimpleModule()
                .addDeserializer(int.class, new StrictIntDeserializer())
                .addDeserializer(Integer, new StrictIntDeserializer())
        )
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
                'person2': new Person('Camilla', 'Drinkwater', 20, 118.0)
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
                'person1': new Person(),
                'person2': new Person('Camilla', 'Drinkwater', 20, 118.0)
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

    def 'test that handleWeirdStringValue logs correctly and returns default value on error'() {
        given:
        def json = mapper.writeValueAsString([
                'name': 'Alex',
                'lastname': 'Fulminazzo',
                'age': 23,
                'income': 'invalid'
        ])

        when:
        def value = mapper.readValue(json, Person)

        then:
        noExceptionThrown()

        and:
        value == new Person()

        and:
        1 * logger.warn('Invalid value for property \'income\': expected double but got \'invalid\' (path: \'income\')')
        1 * logger.warn('Using default value: {}', 0.0)
    }

    def 'test that handleWeirdNumberValue logs correctly and returns default value on error'() {
        given:
        def json = mapper.writeValueAsString([
                'name': 'Alex',
                'lastname': 'Fulminazzo',
                'age': Long.MAX_VALUE
        ])

        when:
        def value = mapper.readValue(json, Person)

        then:
        noExceptionThrown()

        and:
        value == new Person()

        and:
        1 * logger.warn('Invalid value for property \'age\': expected int but got \'9223372036854775807\' (path: \'age\')')
        1 * logger.warn('Using default value: {}', 23)
    }

    def 'test that handleUnexpectedToken logs correctly and returns default value on error'() {
        given:
        def json = mapper.writeValueAsString([
                'name': 'Alex',
                'lastname': 'Fulminazzo',
                'age': 23,
                'income': [1, 2, 3]
        ])

        when:
        def value = mapper.readValue(json, Person)

        then:
        noExceptionThrown()

        and:
        value == new Person()

        and:
        1 * logger.warn('Invalid value for property \'income\': expected double but got token \'START_ARRAY\' (path: \'income[0]\')')
        1 * logger.warn('Using default value: {}', 0.0)
    }

    private static class StrictIntDeserializer extends StdDeserializer<Integer> {

        StrictIntDeserializer() {
            super(Integer.class)
        }

        @Override
        Integer deserialize(JsonParser p, DeserializationContext context) throws IOException {
            def number = p.getNumberValue()

            def longValue = number.longValue()
            if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
                def result = context.handleWeirdNumberValue(
                        Integer,
                        number,
                        "Numeric value (" + number + ") out of range of int"
                )

                if (result instanceof Integer) return (Integer) result
                else return null
            }
            return number.intValue()
        }

    }

}
