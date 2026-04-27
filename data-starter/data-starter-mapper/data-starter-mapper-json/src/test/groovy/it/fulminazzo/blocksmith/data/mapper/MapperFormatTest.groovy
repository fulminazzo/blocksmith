package it.fulminazzo.blocksmith.data.mapper

import spock.lang.Specification

class MapperFormatTest extends Specification {

    def 'test that newMapper of JSON works'() {
        when:
        def actual = MapperFormat.JSON.newMapper()

        then:
        (actual instanceof SerializableMapper)
    }

}