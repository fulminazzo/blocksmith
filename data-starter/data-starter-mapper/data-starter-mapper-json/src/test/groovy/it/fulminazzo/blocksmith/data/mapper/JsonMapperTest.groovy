package it.fulminazzo.blocksmith.data.mapper

import it.fulminazzo.blocksmith.data.Car
import spock.lang.Specification

class JsonMapperTest extends Specification {

    def 'test serialize-deserialize cycle'() {
        given:
        def mapper = new JsonMapper()
        def expected = new Car('Audi', 200.0d, Car.Fuel.DIESEL)

        when:
        def serialized = mapper.serialize(expected)

        then:
        serialized == '{"brand":"Audi","maxSpeed":200.0,"fuel":"DIESEL"}'

        when:
        def actual = mapper.deserialize(serialized, Car)

        then:
        actual == expected
    }

    def 'test that serialize throws JsonException on IOException'() {
        given:
        def mapper = new JsonMapper()

        when:
        mapper.serialize(new Unserializable())

        then:
        thrown(JsonMapper.JsonException)
    }

    def 'test that deserialize throws JsonException on IOException'() {
        given:
        def mapper = new JsonMapper()

        when:
        mapper.deserialize('invalid_data', Unserializable)

        then:
        thrown(JsonMapper.JsonException)
    }

    private static class Unserializable {

    }

}
