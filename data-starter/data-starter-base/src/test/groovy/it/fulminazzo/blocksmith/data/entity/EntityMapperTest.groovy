package it.fulminazzo.blocksmith.data.entity

import spock.lang.Specification

class EntityMapperTest extends Specification {

    def 'test that entity mapper correctly maps #expectedType'() {
        expect:
        mapper.type == expectedType

        when:
        def id = mapper.getId(object)

        then:
        id == expectedId

        where:
        mapper                                                   | object      || expectedType | expectedId
        EntityMapper.create(SimpleEntity, SimpleEntity::getUuid) |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || SimpleEntity | UUID.nameUUIDFromBytes('Alex'.bytes)
    }

}
