package it.fulminazzo.config.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonStreamContext
import spock.lang.Specification
import spock.mock.MockMakers

class JacksonUtilsTest extends Specification {

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
