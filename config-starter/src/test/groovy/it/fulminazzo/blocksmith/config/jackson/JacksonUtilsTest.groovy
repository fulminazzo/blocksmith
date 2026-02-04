package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import spock.lang.Specification
import spock.mock.MockMakers

class JacksonUtilsTest extends Specification {

    def 'test that mapper does not throw on constraint violation'() {
        given:
        def logger = Mock(Logger)
        def mapper = JacksonUtils.setupMapper(new ObjectMapper(), logger)

        and:
        def json = mapper.writeValueAsString(data)

        when:
        def value = mapper.readValue(json, Person)

        then:
        noExceptionThrown()

        and:
        value == new Person()

        and:
        1 * logger.warn('Invalid value for property \'{}\': {} (path: {})',
                _, message, _
        )
        0 * logger.debug('Invalid value for property \'{}\': {} (path: {})',
                _, message, _, _
        )
        1 * logger.warn('Using default value: {}', defaultValue)

        where:
        data                                                                 || message                            | defaultValue
        ['name': null, 'lastname': null, 'age': 17, 'income': -1]            || 'name cannot be null or empty'     | 'Alex'
        ['name': '', 'lastname': null, 'age': 17, 'income': -1]              || 'name cannot be null or empty'     | 'Alex'
        ['name': '     ', 'lastname': null, 'age': 17, 'income': -1]         || 'name cannot be null or empty'     | 'Alex'
        ['name': 'Alex', 'lastname': null, 'age': 17, 'income': -1]          || 'lastname cannot be null or empty' | 'Fulminazzo'
        ['name': 'Alex', 'lastname': '', 'age': 17, 'income': -1]            || 'lastname cannot be null or empty' | 'Fulminazzo'
        ['name': 'Alex', 'lastname': '     ', 'age': 17, 'income': -1]       || 'lastname cannot be null or empty' | 'Fulminazzo'
        ['name': 'Alex', 'lastname': 'Fulminazzo', 'age': 17, 'income': -1]  || 'minimum age must be 18 years'     | 23
        ['name': 'Alex', 'lastname': 'Fulminazzo', 'age': 111, 'income': -1] || 'maximum age must be 110 years'    | 23
        ['name': 'Alex', 'lastname': 'Fulminazzo', 'age': 23, 'income': -1]  || 'income cannot be negative'        | 0.0
    }

    def 'test that mapper does not throw on exception'() {
        given:
        def logger = Mock(Logger)
        def mapper = JacksonUtils.setupMapper(new ObjectMapper(), logger)

        and:
        def json = mapper.writeValueAsString([
                'name'    : 'Alex',
                'lastname': 'Fulminazzo',
                'age'     : 9999999999999999,
                'income'  : 0.0
        ])

        when:
        def value = mapper.readValue(json, Person)

        then:
        noExceptionThrown()

        and:
        value == new Person()

        and:
        1 * logger.warn('Invalid value for property \'{}\': {} (path: {})',
                'age', 'Numeric value (9999999999999999) out of range of int (-2147483648 - 2147483647)', 'age'
        )
        1 * logger.debug('Invalid value for property \'{}\': {} (path: {})',
                'age', 'Numeric value (9999999999999999) out of range of int (-2147483648 - 2147483647)', 'age', _
        )
        1 * logger.warn('Using default value: {}', 23)
    }

    def 'test that getCurrentPath returns expected'() {
        given:
        def parser = Mock(JsonParser)

        and:
        def property = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        property.inArray() >> false
        property.currentName >> 'property'
        parser.parsingContext >> property

        def object = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        object.inArray() >> false
        object.currentName >> 'object'
        property.parent >> object

        def array = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        array.inArray() >> true
        array.currentIndex >> 3
        object.parent >> array

        def container = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        container.inArray() >> false
        container.currentName >> 'container'
        array.parent >> container

        def root = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        root.inArray() >> false
        root.currentName >> null
        container.parent >> root

        when:
        def path = JacksonUtils.getCurrentPath(parser)

        then:
        path == 'container[3].object.property'
    }

    def 'test that getCurrentPath of root returns empty'() {
        given:
        def parser = Mock(JsonParser)

        and:

        def root = Mock(JsonStreamContext, mockMaker: MockMakers.mockito)
        root.inArray() >> false
        root.currentName >> null
        parser.parsingContext >> root

        when:
        def path = JacksonUtils.getCurrentPath(parser)

        then:
        path == ''
    }

}
