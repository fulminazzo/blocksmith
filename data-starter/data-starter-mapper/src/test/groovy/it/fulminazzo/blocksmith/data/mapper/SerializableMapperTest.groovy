package it.fulminazzo.blocksmith.data.mapper

import it.fulminazzo.blocksmith.data.Car
import spock.lang.Specification

class SerializableMapperTest extends Specification {

    def 'test serialize-deserialize cycle'() {
        given:
        def mapper = new SerializableMapper()
        def expected = new Car('Audi', 200.0d, Car.Fuel.DIESEL)

        when:
        def serialized = mapper.serialize(expected)

        then:
        serialized == 'rO0ABXNyACFpdC5mdWxtaW5henpvLmJsb2Nrc21pdGguZGF0YS5DYXJD' +
                'RlwbZiKoVAIAA0QACG1heFNwZWVkTAAFYnJhbmR0ABJMamF2YS9sYW5nL1N0cm' +
                'luZztMAARmdWVsdAAoTGl0L2Z1bG1pbmF6em8vYmxvY2tzbWl0aC9kYXRhL0Nh' +
                'ciRGdWVsO3hwQGkAAAAAAAB0AARBdWRpfnIAJml0LmZ1bG1pbmF6em8uYmxvY2' +
                'tzbWl0aC5kYXRhLkNhciRGdWVsAAAAAAAAAAASAAB4cgAOamF2YS5sYW5nLkVu' +
                'dW0AAAAAAAAAABIAAHhwdAAGRElFU0VM'

        when:
        def actual = mapper.deserialize(serialized, Car)

        then:
        actual == expected
    }

    def 'test that serialize throws SerializationException on IOException'() {
        given:
        def mapper = new SerializableMapper()

        when:
        mapper.serialize(new Unserializable())

        then:
        thrown(MapperException)
    }

    def 'test that deserialize throws SerializationException on IOException'() {
        given:
        def mapper = new SerializableMapper()

        when:
        mapper.deserialize('invaliddata', Unserializable)

        then:
        thrown(MapperException)
    }

    private static class Unserializable {

    }

}
