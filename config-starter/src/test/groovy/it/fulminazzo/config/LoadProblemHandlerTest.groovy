package it.fulminazzo.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import spock.lang.Specification

class LoadProblemHandlerTest extends Specification {
    private Logger logger
    private ObjectMapper mapper

    void setup() {
        logger = Mock()
        mapper = new ObjectMapper()
                .addHandler(new JacksonConfigurationAdapter.LoadProblemHandler(logger))
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
        def typeFactory = mapper.getTypeFactory();
        def mapType = typeFactory.constructMapType(
                Map.class,
                String.class,
                Person.class
        );

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

}
