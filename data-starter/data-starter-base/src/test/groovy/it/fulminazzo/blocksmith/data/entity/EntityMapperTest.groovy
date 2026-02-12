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
        mapper                                                   | object       || expectedType  | expectedId
        EntityMapper.create(ValidIdEntity)                       |
                new ValidIdEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || ValidIdEntity | UUID.nameUUIDFromBytes('Alex'.bytes)
        EntityMapper.create(IdFieldEntity)                       |
                new IdFieldEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex') || IdFieldEntity | UUID.nameUUIDFromBytes('Alex'.bytes)
        EntityMapper.create(SimpleEntity, 'uuid')                |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex')  || SimpleEntity  | UUID.nameUUIDFromBytes('Alex'.bytes)
        EntityMapper.create(SimpleEntity, 'name')                |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex')  || SimpleEntity  | 'Alex'
        EntityMapper.create(SimpleEntity, SimpleEntity::getUuid) |
                new SimpleEntity(UUID.nameUUIDFromBytes('Alex'.bytes), 'Alex')  || SimpleEntity  | UUID.nameUUIDFromBytes('Alex'.bytes)
    }

    def 'test that create(Class) throws IllegalArgumentException on #type'() {
        when:
        EntityMapper.create(type)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == message

        where:
        type            || message
        InvalidIdEntity || "Invalid entity '${InvalidIdEntity.canonicalName}'." +
                " Detected 2 annotated fields with ${Id.simpleName}. Please choose only one field"
        SimpleEntity    || "Invalid entity '${SimpleEntity.canonicalName}'. " +
                "Could not find field '${EntityMapper.defaultIdFieldName}' and no field annotated with ${Id.simpleName} was present"
    }

    def 'test that create(Class, String) throws IllegalArgumentException if could not find argument'() {
        when:
        EntityMapper.create(SimpleEntity, 'invalid')

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Could not find field 'invalid' in ${SimpleEntity.canonicalName}"
    }

}
