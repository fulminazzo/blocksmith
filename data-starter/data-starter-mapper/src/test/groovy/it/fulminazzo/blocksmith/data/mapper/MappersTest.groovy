package it.fulminazzo.blocksmith.data.mapper

import it.fulminazzo.blocksmith.reflect.ReflectException
import spock.lang.Specification

class MappersTest extends Specification {

    def 'test that json mapper throws on serialize'() {
        given:
        def mapper = Mappers.JSON

        when:
        mapper.serialize('Hello, world!')

        then:
        def e = thrown(MapperException)
        e.message == "Could not find suitable ${Mapper.canonicalName} for json. " +
                "Please check that the module it.fulminazzo.blocksmith:data-starter-mapper-json is correctly installed."
    }

    def 'test that json mapper throws on deserialize'() {
        given:
        def mapper = Mappers.JSON

        when:
        mapper.deserialize('Hello, world!', String)

        then:
        def e = thrown(MapperException)
        e.message == "Could not find suitable ${Mapper.canonicalName} for json. " +
                "Please check that the module it.fulminazzo.blocksmith:data-starter-mapper-json is correctly installed."
    }

    def 'test that getMapper throws ReflectException for missing constructor'() {
        when:
        Mappers.getMapper('NoMethod')

        then:
        def e = thrown(ReflectException)
        e.message == "Could not find constructor with types () in type '${NoMethodMapper.canonicalName}'"
    }

}
