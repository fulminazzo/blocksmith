package it.fulminazzo.blocksmith.data.mapper

import spock.lang.Specification

class MapperFormatTest extends Specification {

    def 'test that newMapper of SERIALIZABLE works'() {
        when:
        def actual = MapperFormat.SERIALIZABLE.newMapper()

        then:
        (actual instanceof SerializableMapper)
    }

    def 'test that newMapper of JSON throws if not found'() {
        given:
        def mapperFormat = MapperFormat.JSON

        when:
        mapperFormat.newMapper()

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${Mapper.simpleName} for ${mapperFormat.name().toLowerCase().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:data-starter-mapper-${mapperFormat.name().toLowerCase()} " +
                "is correctly installed."
    }

}
