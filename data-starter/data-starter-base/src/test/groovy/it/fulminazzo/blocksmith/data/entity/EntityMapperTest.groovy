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
        EntityMapper.create(SimpleEntity, 'uuid') |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || SimpleEntity | UUID.nameUUIDFromBytes('Alex'.bytes)
        EntityMapper.create(SimpleEntity, 'name') |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || SimpleEntity | 'Alex'
        EntityMapper.create(SimpleEntity, SimpleEntity::getUuid) |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || SimpleEntity | UUID.nameUUIDFromBytes('Alex'.bytes)
    }

    def 'test that entity mapper throws IllegalArgumentException if could not find argument'() {
        when:
        EntityMapper.create(SimpleEntity, 'invalid')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Could not find field 'invalid' in ${SimpleEntity.canonicalName}"
    }

}
