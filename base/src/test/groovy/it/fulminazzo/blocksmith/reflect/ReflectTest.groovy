package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.Field
import java.util.function.Predicate

class ReflectTest extends Specification {
    private static final Field DEFAULT_NAME = NamedEntity.getDeclaredField('DEFAULT_NAME')
    private static final Field DEFAULT_AGE = Person.getDeclaredField('DEFAULT_AGE')
    private static final Field name = NamedEntity.getDeclaredField('name')
    private static final Field age = Person.getDeclaredField('age')

    private static final String nameValue = 'Alex'
    private static final int ageValue = 23

    static {
        DEFAULT_NAME.accessible = true
        DEFAULT_AGE.accessible = true
    }

    private Reflect reflect

    void setup() {
        reflect = new Reflect(Person, new Person(nameValue, ageValue))
    }

    def 'test that #method with #arguments returns #expected'() {
        when:
        def actual = reflect."$method"(*arguments)

        then:
        actual == expected

        where:
        method                    | arguments                                                     || expected
        // getFieldsObject
        'getInstanceFieldsObject' | []                                                            || [ageValue, nameValue].collect { new Reflect(it.class, it) }
        'getStaticFieldsObject'   | []                                                            || [DEFAULT_AGE.get(null), DEFAULT_NAME.get(null)].collect { new Reflect(it.class, it) }
        'getFieldsObject'         | [((Predicate<Field>) (f) -> false)]                           || [].collect { new Reflect(it.class, it) }
        'getFieldsObject'         | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)] || [DEFAULT_NAME.get(null), nameValue].collect { new Reflect(it.class, it) }
        'getFieldsObject'         | [((Predicate<Field>) (f) -> f.declaringClass == Person)]      || [DEFAULT_AGE.get(null), ageValue].collect { new Reflect(it.class, it) }
        'getFieldsObject'         | [((Predicate<Field>) (f) -> true)]                            || [DEFAULT_AGE.get(null), ageValue, DEFAULT_NAME.get(null), nameValue].collect { new Reflect(it.class, it) }
        'getFieldsObject'         | []                                                            || [DEFAULT_AGE.get(null), ageValue, DEFAULT_NAME.get(null), nameValue].collect { new Reflect(it.class, it) }
        // getFieldObject
        'getInstanceFieldObject'  | [name.name]                                                   || new Reflect(name.type, nameValue)
        'getInstanceFieldObject'  | [age.name]                                                    || new Reflect(age.type, ageValue)
        'getStaticFieldObject'    | [DEFAULT_NAME.name]                                           || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getStaticFieldObject'    | [DEFAULT_AGE.name]                                            || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'getFieldObject'          | [DEFAULT_NAME.name]                                           || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getFieldObject'          | [DEFAULT_AGE.name]                                            || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'getFieldObject'          | [name.name]                                                   || new Reflect(name.type, nameValue)
        'getFieldObject'          | [age.name]                                                    || new Reflect(age.type, ageValue)
        'getFieldObject'          | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)] || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getFieldObject'          | [((Predicate<Field>) (f) -> f.declaringClass == Person)]      || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'getFieldObject'          | [((Predicate<Field>) (f) -> true)]                            || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        // getField
        'getInstanceField'        | [name.name]                                                   || name
        'getInstanceField'        | [age.name]                                                    || age
        'getStaticField'          | [DEFAULT_NAME.name]                                           || DEFAULT_NAME
        'getStaticField'          | [DEFAULT_AGE.name]                                            || DEFAULT_AGE
        'getField'                | [DEFAULT_NAME.name]                                           || DEFAULT_NAME
        'getField'                | [DEFAULT_AGE.name]                                            || DEFAULT_AGE
        'getField'                | [name.name]                                                   || name
        'getField'                | [age.name]                                                    || age
        'getField'                | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)] || DEFAULT_NAME
        'getField'                | [((Predicate<Field>) (f) -> f.declaringClass == Person)]      || DEFAULT_AGE
        'getField'                | [((Predicate<Field>) (f) -> true)]                            || DEFAULT_AGE
        // getFields
        'getInstanceFields'       | []                                                            || [age, name]
        'getStaticFields'         | []                                                            || [DEFAULT_AGE, DEFAULT_NAME]
        'getFields'               | [((Predicate<Field>) (f) -> false)]                           || []
        'getFields'               | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)] || [DEFAULT_NAME, name]
        'getFields'               | [((Predicate<Field>) (f) -> f.declaringClass == Person)]      || [DEFAULT_AGE, age]
        'getFields'               | [((Predicate<Field>) (f) -> true)]                            || [DEFAULT_AGE, age, DEFAULT_NAME, name]
        'getFields'               | []                                                            || [DEFAULT_AGE, age, DEFAULT_NAME, name]
    }

    def 'test that #method with #arguments throws ReflectException with #expected'() {
        when:
        reflect."$method"(*arguments)

        then:
        def e = thrown(ReflectException)
        e.message == expected.message

        where:
        method                   | arguments                                                || expected
        // getFieldObject
        'getInstanceFieldObject' | [DEFAULT_NAME.name]                                      || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getInstanceFieldObject' | [DEFAULT_AGE.name]                                       || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getInstanceFieldObject' | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getStaticFieldObject'   | [name.name]                                              || ReflectException.cannotFindField(Person, name.name)
        'getStaticFieldObject'   | [age.name]                                               || ReflectException.cannotFindField(Person, age.name)
        'getStaticFieldObject'   | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getFieldObject'         | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getFieldObject'         | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getFieldObject'         | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getFieldObject'         | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getFieldObject'         | [((Predicate<Field>) (f) -> f.declaringClass == String)] || ReflectException.cannotFindField(Person)
        // getField
        'getInstanceField'       | [DEFAULT_NAME.name]                                      || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getInstanceField'       | [DEFAULT_AGE.name]                                       || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getInstanceField'       | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getStaticField'         | [name.name]                                              || ReflectException.cannotFindField(Person, name.name)
        'getStaticField'         | [age.name]                                               || ReflectException.cannotFindField(Person, age.name)
        'getStaticField'         | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'               | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'               | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'               | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'               | ['notExisting']                                          || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'               | [((Predicate<Field>) (f) -> f.declaringClass == String)] || ReflectException.cannotFindField(Person)
        'getField'               | [((Predicate<Field>) (f) -> false)]                      || ReflectException.cannotFindField(Person)
    }

    def 'test that getField object throws ReflectException on IllegalAccessException'() {
        given:
        def field = Mock(Field)
        field.get(_) >> {
            throw new IllegalAccessException()
        }

        when:
        reflect.getFieldObject(field)

        then:
        thrown(ReflectException)
    }

    def 'test that cast of #type to #object returns #expected'() {
        when:
        def actual = Reflect.cast(type, object)

        then:
        actual == expected
        actual.class == expected.class

        where:
        type      | object                  || expected
        // byte
        byte      | 1 as byte               || 1 as byte
        byte      | 1 as Byte               || 1 as byte
        byte      | 1 as short              || 1 as byte
        byte      | 1 as Short              || 1 as byte
        byte      | 1 as int                || 1 as byte
        byte      | 1 as Integer            || 1 as byte
        byte      | 1 as long               || 1 as byte
        byte      | 1 as Long               || 1 as byte
        byte      | 1 as float              || 1 as byte
        byte      | 1 as Float              || 1 as byte
        byte      | 1 as double             || 1 as byte
        byte      | 1 as Double             || 1 as byte
        byte      | 'a' as char             || 97 as byte
        byte      | 'a' as Character        || 97 as byte
        // Byte
        Byte      | 1 as byte               || 1 as byte
        Byte      | 1 as Byte               || 1 as byte
        Byte      | 1 as short              || 1 as byte
        Byte      | 1 as Short              || 1 as byte
        Byte      | 1 as int                || 1 as byte
        Byte      | 1 as Integer            || 1 as byte
        Byte      | 1 as long               || 1 as byte
        Byte      | 1 as Long               || 1 as byte
        Byte      | 1 as float              || 1 as byte
        Byte      | 1 as Float              || 1 as byte
        Byte      | 1 as double             || 1 as byte
        Byte      | 1 as Double             || 1 as byte
        Byte      | 'a' as char             || 97 as byte
        Byte      | 'a' as Character        || 97 as byte
        // char
        char      | 1 as byte               || (char) 1
        char      | 1 as Byte               || (char) 1
        char      | 1 as short              || (char) 1
        char      | 1 as Short              || (char) 1
        char      | 1 as int                || (char) 1
        char      | 1 as Integer            || (char) 1
        char      | 1 as long               || (char) 1
        char      | 1 as Long               || (char) 1
        char      | 1 as float              || (char) 1
        char      | 1 as Float              || (char) 1
        char      | 1 as double             || (char) 1
        char      | 1 as Double             || (char) 1
        char      | 'a' as char             || 'a' as char
        char      | 'a' as Character        || 'a' as char
        // Character
        Character | 1 as byte               || (char) 1
        Character | 1 as Byte               || (char) 1
        Character | 1 as short              || (char) 1
        Character | 1 as Short              || (char) 1
        Character | 1 as int                || (char) 1
        Character | 1 as Integer            || (char) 1
        Character | 1 as long               || (char) 1
        Character | 1 as Long               || (char) 1
        Character | 1 as float              || (char) 1
        Character | 1 as Float              || (char) 1
        Character | 1 as double             || (char) 1
        Character | 1 as Double             || (char) 1
        Character | 'a' as char             || 'a' as char
        Character | 'a' as Character        || 'a' as char
        // short
        short     | 1 as byte               || 1 as short
        short     | 1 as Byte               || 1 as short
        short     | 1 as short              || 1 as short
        short     | 1 as Short              || 1 as short
        short     | 1 as int                || 1 as short
        short     | 1 as Integer            || 1 as short
        short     | 1 as long               || 1 as short
        short     | 1 as Long               || 1 as short
        short     | 1 as float              || 1 as short
        short     | 1 as Float              || 1 as short
        short     | 1 as double             || 1 as short
        short     | 1 as Double             || 1 as short
        short     | 'a' as char             || 97 as short
        short     | 'a' as Character        || 97 as short
        // Short
        Short     | 1 as byte               || 1 as short
        Short     | 1 as Byte               || 1 as short
        Short     | 1 as short              || 1 as short
        Short     | 1 as Short              || 1 as short
        Short     | 1 as int                || 1 as short
        Short     | 1 as Integer            || 1 as short
        Short     | 1 as long               || 1 as short
        Short     | 1 as Long               || 1 as short
        Short     | 1 as float              || 1 as short
        Short     | 1 as Float              || 1 as short
        Short     | 1 as double             || 1 as short
        Short     | 1 as Double             || 1 as short
        Short     | 'a' as char             || 97 as short
        Short     | 'a' as Character        || 97 as short
        // int
        int       | 1 as byte               || 1 as int
        int       | 1 as Byte               || 1 as int
        int       | 1 as short              || 1 as int
        int       | 1 as Short              || 1 as int
        int       | 1 as int                || 1 as int
        int       | 1 as Integer            || 1 as int
        int       | 1 as long               || 1 as int
        int       | 1 as Long               || 1 as int
        int       | 1 as float              || 1 as int
        int       | 1 as Float              || 1 as int
        int       | 1 as double             || 1 as int
        int       | 1 as Double             || 1 as int
        int       | 'a' as char             || 97 as int
        int       | 'a' as Character        || 97 as int
        // Integer
        Integer   | 1 as byte               || 1 as int
        Integer   | 1 as Byte               || 1 as int
        Integer   | 1 as short              || 1 as int
        Integer   | 1 as Short              || 1 as int
        Integer   | 1 as int                || 1 as int
        Integer   | 1 as Integer            || 1 as int
        Integer   | 1 as long               || 1 as int
        Integer   | 1 as Long               || 1 as int
        Integer   | 1 as float              || 1 as int
        Integer   | 1 as Float              || 1 as int
        Integer   | 1 as double             || 1 as int
        Integer   | 1 as Double             || 1 as int
        Integer   | 'a' as char             || 97 as int
        Integer   | 'a' as Character        || 97 as int
        // long
        long      | 1 as byte               || 1 as long
        long      | 1 as Byte               || 1 as long
        long      | 1 as short              || 1 as long
        long      | 1 as Short              || 1 as long
        long      | 1 as int                || 1 as long
        long      | 1 as Integer            || 1 as long
        long      | 1 as long               || 1 as long
        long      | 1 as Long               || 1 as long
        long      | 1 as float              || 1 as long
        long      | 1 as Float              || 1 as long
        long      | 1 as double             || 1 as long
        long      | 1 as Double             || 1 as long
        long      | 'a' as char             || 97 as long
        long      | 'a' as Character        || 97 as long
        // Long
        Long      | 1 as byte               || 1 as long
        Long      | 1 as Byte               || 1 as long
        Long      | 1 as short              || 1 as long
        Long      | 1 as Short              || 1 as long
        Long      | 1 as int                || 1 as long
        Long      | 1 as Integer            || 1 as long
        Long      | 1 as long               || 1 as long
        Long      | 1 as Long               || 1 as long
        Long      | 1 as float              || 1 as long
        Long      | 1 as Float              || 1 as long
        Long      | 1 as double             || 1 as long
        Long      | 1 as Double             || 1 as long
        Long      | 'a' as char             || 97 as long
        Long      | 'a' as Character        || 97 as long
        // float
        float     | 1 as byte               || 1 as float
        float     | 1 as Byte               || 1 as float
        float     | 1 as short              || 1 as float
        float     | 1 as Short              || 1 as float
        float     | 1 as int                || 1 as float
        float     | 1 as Integer            || 1 as float
        float     | 1 as long               || 1 as float
        float     | 1 as Long               || 1 as float
        float     | 1 as float              || 1 as float
        float     | 1 as Float              || 1 as float
        float     | 1 as double             || 1 as float
        float     | 1 as Double             || 1 as float
        float     | 'a' as char             || 97 as float
        float     | 'a' as Character        || 97 as float
        // Float
        Float     | 1 as byte               || 1 as float
        Float     | 1 as Byte               || 1 as float
        Float     | 1 as short              || 1 as float
        Float     | 1 as Short              || 1 as float
        Float     | 1 as int                || 1 as float
        Float     | 1 as Integer            || 1 as float
        Float     | 1 as long               || 1 as float
        Float     | 1 as Long               || 1 as float
        Float     | 1 as float              || 1 as float
        Float     | 1 as Float              || 1 as float
        Float     | 1 as double             || 1 as float
        Float     | 1 as Double             || 1 as float
        Float     | 'a' as char             || 97 as float
        Float     | 'a' as Character        || 97 as float
        // double
        double    | 1 as byte               || 1 as double
        double    | 1 as Byte               || 1 as double
        double    | 1 as short              || 1 as double
        double    | 1 as Short              || 1 as double
        double    | 1 as int                || 1 as double
        double    | 1 as Integer            || 1 as double
        double    | 1 as long               || 1 as double
        double    | 1 as Long               || 1 as double
        double    | 1 as float              || 1 as double
        double    | 1 as Float              || 1 as double
        double    | 1 as double             || 1 as double
        double    | 1 as Double             || 1 as double
        double    | 'a' as char             || 97 as double
        double    | 'a' as Character        || 97 as double
        // Double
        Double    | 1 as byte               || 1 as double
        Double    | 1 as Byte               || 1 as double
        Double    | 1 as short              || 1 as double
        Double    | 1 as Short              || 1 as double
        Double    | 1 as int                || 1 as double
        Double    | 1 as Integer            || 1 as double
        Double    | 1 as long               || 1 as double
        Double    | 1 as Long               || 1 as double
        Double    | 1 as float              || 1 as double
        Double    | 1 as Float              || 1 as double
        Double    | 1 as double             || 1 as double
        Double    | 1 as Double             || 1 as double
        Double    | 'a' as char             || 97 as double
        Double    | 'a' as Character        || 97 as double
        // boolean
        boolean   | true as boolean         || true as boolean
        boolean   | Boolean.TRUE as Boolean || true as Boolean
        // Boolean
        Boolean   | true as boolean         || true as boolean
        Boolean   | Boolean.TRUE as Boolean || true as Boolean
        // String
        String    | 'Hello, world!'         || 'Hello, world!'
    }

}
