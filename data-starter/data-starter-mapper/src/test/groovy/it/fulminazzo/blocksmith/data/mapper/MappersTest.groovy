package it.fulminazzo.blocksmith.data.mapper

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

    def 'test that getMapper of #prefix throws #expected'() {
        when:
        Mappers.getMapper(prefix)

        then:
        def e = thrown(expected.class)
        e.message == expected.message

        and:
        def eCause = expected.cause
        def cause = e.cause

        if (eCause == null) assert cause == null
        else {
            assert cause != null
            assert cause.class == eCause.class
            assert cause.message == eCause.message
        }

        where:
        prefix             || expected
        'NoMethod'         || new IllegalArgumentException(
                "Could not find constructor ${NoMethodMapper.canonicalName}()"
        )
        'RuntimeException' || new RuntimeException('Test runtime exception')
        'Exception'        || new RuntimeException(new Exception('Test exception'))
    }

}
