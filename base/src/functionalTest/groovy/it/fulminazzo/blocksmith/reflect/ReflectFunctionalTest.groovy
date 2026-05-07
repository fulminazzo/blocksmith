package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

@SuppressWarnings('GroovyAccessibility')
class ReflectFunctionalTest extends Specification {
    private static final Constructor<?> CONSTRUCTOR = Person.getDeclaredConstructor(String, Integer)

    /*
     * TEST SUBJECTS
     */
    private static final Field INTERFACE_STATIC_FIELD = Entity.getDeclaredField('ENTITIES_DEFAULT_NAME')
    private static final Field SUPER_STATIC_FIELD = NamedEntity.getDeclaredField('defaultName')
    private static final Field STATIC_FIELD = Person.getDeclaredField('defaultAge')
    private static final Field SUPER_FIELD = NamedEntity.getDeclaredField('name')
    private static final Field FIELD = Person.getDeclaredField('age')

    private static final Method INTERFACE_DEFAULT_METHOD_NO_ARGS = Entity.getDeclaredMethod('getUniqueId')
    private static final Method INTERFACE_METHOD_NO_ARGS = Entity.getDeclaredMethod('getName')
    private static final Method SUPER_STATIC_METHOD_NO_ARGS = NamedEntity.getDeclaredMethod('getDefaultName')
    private static final Method STATIC_METHOD_NO_ARGS = Person.getDeclaredMethod('getDefaultAge')
    private static final Method SUPER_METHOD_NO_ARGS = NamedEntity.getDeclaredMethod('getName')
    private static final Method METHOD_NO_ARGS = Person.getDeclaredMethod('getAge')

    private static final Method SUPER_STATIC_METHOD = NamedEntity.getDeclaredMethod('setDefaultName', String)
    private static final Method STATIC_METHOD = Person.getDeclaredMethod('setDefaultAge', Integer)
    private static final Method SUPER_METHOD = NamedEntity.getDeclaredMethod('setName', String)
    private static final Method METHOD = Person.getDeclaredMethod('setAge', Integer)

    private static final Method SUPER_EQUALS = NamedEntity.getDeclaredMethod('equals', Object)
    private static final Method SUPER_HASH_CODE = NamedEntity.getDeclaredMethod('hashCode')
    private static final Method SUPER_TO_STRING = NamedEntity.getDeclaredMethod('toString')
    private static final Method SUPER_LOMBOK_CAN_EQUAL = NamedEntity.getDeclaredMethod('canEqual', Object)

    private static final Method EQUALS = Person.getDeclaredMethod('equals', Object)
    private static final Method HASH_CODE = Person.getDeclaredMethod('hashCode')
    private static final Method TO_STRING = Person.getDeclaredMethod('toString')
    private static final Method LOMBOK_CAN_EQUAL = Person.getDeclaredMethod('canEqual', Object)

    /*
     * TEST ARGUMENTS
     */
    private static final Predicate<Field> IN_INTERFACE = (Predicate<Field>) (f) -> f.declaringClass == Entity
    private static final Predicate<Field> IN_SUPER = (Predicate<Field>) (f) -> f.declaringClass == NamedEntity
    private static final Predicate<Field> IN_INSTANCE = (Predicate<Field>) (f) -> f.declaringClass == Person
    private static final Predicate<Field> TRUE_PREDICATE = (Predicate<Field>) (f) -> true
    private static final Predicate<Field> FALSE_PREDICATE = (Predicate<Field>) (f) -> false
    private static final Predicate<Field> IN_OTHER = (Predicate<Field>) (f) -> f.declaringClass == String

    /*
     * TEST RESULTS
     */
    private static final Object EXPECTED_INTERFACE_STATIC_FIELD_VALUE = INTERFACE_STATIC_FIELD.get(null)
    private static final Object EXPECTED_SUPER_STATIC_FIELD_VALUE = SUPER_STATIC_FIELD.get(null)
    private static final Object EXPECTED_STATIC_FIELD_VALUE = STATIC_FIELD.get(null)

    private static final List<Method> EXPECTED_INTERFACE_METHODS = [INTERFACE_METHOD_NO_ARGS, INTERFACE_DEFAULT_METHOD_NO_ARGS]
    private static final List<Method> EXPECTED_SUPER_METHODS = [
            SUPER_LOMBOK_CAN_EQUAL, SUPER_EQUALS, SUPER_METHOD_NO_ARGS, SUPER_HASH_CODE, SUPER_METHOD, SUPER_TO_STRING
    ]
    private static final List<Method> EXPECTED_METHODS = [LOMBOK_CAN_EQUAL, EQUALS, METHOD_NO_ARGS, HASH_CODE, METHOD, TO_STRING]
    private static final List<Method> OBJECT_METHODS = Object.declaredMethods
            .findAll { !it.synthetic && !it.bridge && !Modifier.isStatic(it.modifiers) }
            .sort { a, b -> a.name <=> b.name ?: a.parameterCount <=> b.parameterCount }

    private static final List<Method> EXPECTED_SUPER_STATIC_METHODS = [SUPER_STATIC_METHOD_NO_ARGS, SUPER_STATIC_METHOD]
    private static final List<Method> EXPECTED_STATIC_METHODS = [STATIC_METHOD_NO_ARGS, STATIC_METHOD]
    private static final List<Method> OBJECT_STATIC_METHODS = Object.declaredMethods
            .findAll { !it.synthetic && !it.bridge && Modifier.isStatic(it.modifiers) }
            .sort { a, b -> a.name <=> b.name ?: a.parameterCount <=> b.parameterCount }

    private static final UUID INTERFACE_VALUE = UUID.nameUUIDFromBytes(SUPER_VALUE.bytes)
    private static final String SUPER_VALUE = 'Alex'
    private static final int VALUE = 23

    static {
        SUPER_STATIC_FIELD.accessible = true
        STATIC_FIELD.accessible = true
    }

    private Reflect reflect

    void setup() {
        reflect = new Reflect(Person, new Person(SUPER_VALUE, VALUE))
    }

    def 'test that isBaseType returns #expected for #type'() {
        given:
        def reflect = new Reflect(type, type)

        when:
        def actual = reflect.baseType

        then:
        actual == expected

        where:
        type      || expected
        byte      || true
        Byte      || true
        char      || true
        Character || true
        short     || true
        Short     || true
        int       || true
        Integer   || true
        long      || true
        Long      || true
        float     || true
        Float     || true
        double    || true
        Double    || true
        boolean   || true
        Boolean   || true
        String    || true
        Object    || false
    }

    def 'test that extendsType works'() {
        given:
        def reflect = new Reflect(object.getClass(), object)

        when:
        def actual = reflect.extendsType(superType)

        then:
        actual == expected

        where:
        object         | superType || expected
        10.toInteger() | Integer   || true
        10.toInteger() | Double    || false
        10.toInteger() | Number    || true
        10.toInteger() | Object    || true
        10d            | Integer   || false
        10d            | Double    || true
        10d            | Number    || true
        10d            | Object    || true
    }

    def 'test that #type extendsType #superType returns #expected'() {
        given:
        def reflect = new Reflect(type, type)

        when:
        def actual = reflect.extendsType(superType)

        then:
        actual == expected

        where:
        type        | superType   || expected
        Person      | NamedEntity || true
        Person      | Class       || true
        Person      | Type        || true
        NamedEntity | Person      || false
        NamedEntity | Class       || true
        NamedEntity | Type        || true
        int         | Integer     || true
        int         | Class       || true
        int         | Type        || true
        int         | Number      || true
        int         | Class       || true
        int         | Type        || true
        Integer     | int         || true
        Integer     | Class       || true
        Integer     | Type        || true
        Number      | int         || false
        Number      | Class       || true
        Number      | Type        || true
    }

    def 'test that cast toWrapper of #type to #object returns #expected'() {
        given:
        def reflect = new Reflect(type, object)

        when:
        def actual = reflect.toWrapper()

        then:
        actual == expected

        where:
        type      | object                  || expected
        // byte
        byte      | 1 as byte               || new Reflect(Byte, 1 as Byte)
        byte      | byte                    || new Reflect(Byte, Byte)
        Byte      | 1 as Byte               || new Reflect(Byte, 1 as Byte)
        Byte      | Byte                    || new Reflect(Byte, Byte)
        // char
        char      | 'a' as char             || new Reflect(Character, 'a' as Character)
        char      | char                    || new Reflect(Character, Character)
        Character | 'a' as Character        || new Reflect(Character, 'a' as Character)
        Character | Character               || new Reflect(Character, Character)
        // short
        short     | 1 as short              || new Reflect(Short, 1 as Short)
        short     | short                   || new Reflect(Short, Short)
        Short     | 1 as Short              || new Reflect(Short, 1 as Short)
        Short     | Short                   || new Reflect(Short, Short)
        // int
        int       | 1                       || new Reflect(Integer, 1)
        int       | int                     || new Reflect(Integer, Integer)
        Integer   | 1 as Integer            || new Reflect(Integer, 1)
        Integer   | Integer                 || new Reflect(Integer, Integer)
        // long
        long      | 1L                      || new Reflect(Long, 1L)
        long      | long                    || new Reflect(Long, Long)
        Long      | 1 as Long               || new Reflect(Long, 1L)
        Long      | Long                    || new Reflect(Long, Long)
        // float
        float     | 1.0f                    || new Reflect(Float, 1.0f)
        float     | float                   || new Reflect(Float, Float)
        Float     | 1 as Float              || new Reflect(Float, 1.0f)
        Float     | Float                   || new Reflect(Float, Float)
        // double
        double    | 1.0d                    || new Reflect(Double, 1.0d)
        double    | double                  || new Reflect(Double, Double)
        Double    | 1 as Double             || new Reflect(Double, 1.0d)
        Double    | Double                  || new Reflect(Double, Double)
        // boolean
        boolean   | true                    || new Reflect(Boolean, true)
        boolean   | boolean                 || new Reflect(Boolean, Boolean)
        Boolean   | Boolean.TRUE as Boolean || new Reflect(Boolean, true)
        Boolean   | Boolean                 || new Reflect(Boolean, Boolean)
        // String
        String    | 'Hello, world!'         || new Reflect(String, 'Hello, world!')
    }

    def 'test that cast toPrimitive of #type to #object returns #expected'() {
        given:
        def reflect = new Reflect(type, object)

        when:
        def actual = reflect.toPrimitive()

        then:
        actual == expected

        where:
        type      | object                  || expected
        // byte
        byte      | 1 as byte               || new Reflect(byte, 1 as byte)
        byte      | byte                    || new Reflect(byte, byte)
        Byte      | 1 as Byte               || new Reflect(byte, 1 as byte)
        Byte      | Byte                    || new Reflect(byte, byte)
        // char
        char      | 'a' as char             || new Reflect(char, 'a' as char)
        char      | char                    || new Reflect(char, char)
        Character | 'a' as Character        || new Reflect(char, 'a' as char)
        Character | Character               || new Reflect(char, char)
        // short
        short     | 1 as short              || new Reflect(short, 1 as short)
        short     | short                   || new Reflect(short, short)
        Short     | 1 as Short              || new Reflect(short, 1 as short)
        Short     | Short                   || new Reflect(short, short)
        // int
        int       | 1                       || new Reflect(int, 1)
        int       | int                     || new Reflect(int, int)
        Integer   | 1 as Integer            || new Reflect(int, 1)
        Integer   | Integer                 || new Reflect(int, int)
        // long
        long      | 1L                      || new Reflect(long, 1L)
        long      | long                    || new Reflect(long, long)
        Long      | 1 as Long               || new Reflect(long, 1L)
        Long      | Long                    || new Reflect(long, long)
        // float
        float     | 1.0f                    || new Reflect(float, 1.0f)
        float     | float                   || new Reflect(float, float)
        Float     | 1 as Float              || new Reflect(float, 1.0f)
        Float     | Float                   || new Reflect(float, float)
        // double
        double    | 1.0d                    || new Reflect(double, 1.0d)
        double    | double                  || new Reflect(double, double)
        Double    | 1 as Double             || new Reflect(double, 1.0d)
        Double    | Double                  || new Reflect(double, double)
        // boolean
        boolean   | true                    || new Reflect(boolean, true)
        boolean   | boolean                 || new Reflect(boolean, boolean)
        Boolean   | Boolean.TRUE as Boolean || new Reflect(boolean, true)
        Boolean   | Boolean                 || new Reflect(boolean, boolean)
        // String
        String    | 'Hello, world!'         || new Reflect(String, 'Hello, world!')
        String    | null                    || new Reflect(String, null)
        // Object
        Object    | null                    || new Reflect(Object, null)
    }

    def 'test that #method with #arguments returns #expected'() {
        when:
        def actual = reflect."$method"(*arguments)

        then:
        actual == expected

        where:
        method                    | arguments                                         || expected
        // init
        'init'                    | ['Camilla', 21]                                   || new Reflect(Person, new Person('Camilla', 21))
        'init'                    | [[null, 21].toArray()]                            || new Reflect(Person, new Person(null, 21))
        'init'                    | ['Camilla', null]                                 || new Reflect(Person, new Person('Camilla', null))
        'init'                    | [[null, null].toArray()]                          || new Reflect(Person, new Person(null, null))
        // getConstructor
        'getConstructor'          | [String, Integer]                                 || CONSTRUCTOR
        'getConstructor'          | [IN_INSTANCE]                                     || CONSTRUCTOR
        'getConstructor'          | [TRUE_PREDICATE]                                  || CONSTRUCTOR
        // getConstructors
        'getConstructors'         | [FALSE_PREDICATE]                                 || []
        'getConstructors'         | [IN_SUPER]                                        || []
        'getConstructors'         | [IN_INSTANCE]                                     || [CONSTRUCTOR]
        'getConstructors'         | [TRUE_PREDICATE]                                  || [CONSTRUCTOR]
        'getConstructors'         | []                                                || [CONSTRUCTOR]
        // getFieldValues
        'getInstanceFieldValues'  | []                                                || [VALUE].collect { new Reflect(it.getClass(), it) }
        'getNonStaticFieldValues' | []                                                || [VALUE, SUPER_VALUE]
                .collect { new Reflect(it.getClass(), it) }
        'getStaticFieldValues'    | []                                                || [
                EXPECTED_STATIC_FIELD_VALUE,
                EXPECTED_SUPER_STATIC_FIELD_VALUE,
                EXPECTED_INTERFACE_STATIC_FIELD_VALUE].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [FALSE_PREDICATE]                                 || [].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [IN_INTERFACE]                                    || [EXPECTED_INTERFACE_STATIC_FIELD_VALUE].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [IN_SUPER]                                        || [EXPECTED_SUPER_STATIC_FIELD_VALUE, SUPER_VALUE].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [IN_INSTANCE]                                     || [EXPECTED_STATIC_FIELD_VALUE, VALUE].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [TRUE_PREDICATE]                                  || [
                EXPECTED_STATIC_FIELD_VALUE, VALUE,
                EXPECTED_SUPER_STATIC_FIELD_VALUE, SUPER_VALUE,
                EXPECTED_INTERFACE_STATIC_FIELD_VALUE].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | []                                                || [
                EXPECTED_STATIC_FIELD_VALUE, VALUE,
                EXPECTED_SUPER_STATIC_FIELD_VALUE, SUPER_VALUE,
                EXPECTED_INTERFACE_STATIC_FIELD_VALUE].collect { new Reflect(it.getClass(), it) }
        // set
        'setInstance'             | [FIELD.name, VALUE]                               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'setNonStatic'            | [SUPER_FIELD.name, SUPER_VALUE]                   || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'setNonStatic'            | [FIELD.name, VALUE]                               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'setStatic'               | [SUPER_STATIC_FIELD.name,
                                     EXPECTED_SUPER_STATIC_FIELD_VALUE]               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'setStatic'               | [STATIC_FIELD.name, EXPECTED_STATIC_FIELD_VALUE]  || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [SUPER_STATIC_FIELD.name,
                                     EXPECTED_SUPER_STATIC_FIELD_VALUE]               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [STATIC_FIELD.name,
                                     EXPECTED_STATIC_FIELD_VALUE]                     || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [SUPER_FIELD.name, SUPER_VALUE]                   || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [FIELD.name, VALUE]                               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [IN_SUPER,
                                     EXPECTED_SUPER_STATIC_FIELD_VALUE]               || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [IN_INSTANCE,
                                     EXPECTED_STATIC_FIELD_VALUE]                     || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        'set'                     | [TRUE_PREDICATE,
                                     EXPECTED_STATIC_FIELD_VALUE]                     || new Reflect(Person, new Person(SUPER_VALUE, VALUE))
        // get orElse
        'getInstance'             | [SUPER_STATIC_FIELD.name, 'unknown']              || new Reflect(String, 'unknown')
        'getInstance'             | [SUPER_STATIC_FIELD.name, null]                   || new Reflect(null, null)
        'getInstance'             | [STATIC_FIELD.name, 15]                           || new Reflect(Integer, 15)
        'getInstance'             | [STATIC_FIELD.name, null]                         || new Reflect(null, null)
        'getInstance'             | [SUPER_FIELD.name, 'unknown']                     || new Reflect(String, 'unknown')
        'getInstance'             | [SUPER_FIELD.name, null]                          || new Reflect(null, null)
        'getInstance'             | [FIELD.name, 15]                                  || new Reflect(FIELD.type, VALUE)
        'getInstance'             | [FIELD.name, null]                                || new Reflect(FIELD.type, VALUE)
        'getNonStatic'            | [SUPER_STATIC_FIELD.name, 'unknown']              || new Reflect(String, 'unknown')
        'getNonStatic'            | [SUPER_STATIC_FIELD.name, null]                   || new Reflect(null, null)
        'getNonStatic'            | [STATIC_FIELD.name, 15]                           || new Reflect(Integer, 15)
        'getNonStatic'            | [STATIC_FIELD.name, null]                         || new Reflect(null, null)
        'getNonStatic'            | [SUPER_FIELD.name, 'unknown']                     || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'getNonStatic'            | [SUPER_FIELD.name, null]                          || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'getNonStatic'            | [FIELD.name, 15]                                  || new Reflect(FIELD.type, VALUE)
        'getNonStatic'            | [FIELD.name, null]                                || new Reflect(FIELD.type, VALUE)
        'getStatic'               | [SUPER_STATIC_FIELD.name, 'unknown']              || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'getStatic'               | [SUPER_STATIC_FIELD.name, null]                   || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'getStatic'               | [INTERFACE_STATIC_FIELD.name, 'unknown']          || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'getStatic'               | [INTERFACE_STATIC_FIELD.name, null]               || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'getStatic'               | [STATIC_FIELD.name, 15]                           || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'getStatic'               | [STATIC_FIELD.name, null]                         || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'getStatic'               | [SUPER_FIELD.name, 'unknown']                     || new Reflect(String, 'unknown')
        'getStatic'               | [SUPER_FIELD.name, null]                          || new Reflect(null, null)
        'getStatic'               | [FIELD.name, 15]                                  || new Reflect(Integer, 15)
        'getStatic'               | [FIELD.name, null]                                || new Reflect(null, null)
        'get'                     | [INTERFACE_STATIC_FIELD.name, 'unknown']          || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'get'                     | [INTERFACE_STATIC_FIELD.name, null]               || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'get'                     | [SUPER_STATIC_FIELD.name, 'unknown']              || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [SUPER_STATIC_FIELD.name, null]                   || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [STATIC_FIELD.name, 15]                           || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [STATIC_FIELD.name, null]                         || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [SUPER_FIELD.name, 'unknown']                     || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'get'                     | [SUPER_FIELD.name, null]                          || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'get'                     | [FIELD.name, 15]                                  || new Reflect(FIELD.type, VALUE)
        'get'                     | [FIELD.name, null]                                || new Reflect(FIELD.type, VALUE)
        'get'                     | ['unknown', 'unknown']                            || new Reflect(String, 'unknown')
        'get'                     | ['unknown', null]                                 || new Reflect(null, null)
        'get'                     | [FALSE_PREDICATE, 'unknown']                      || new Reflect(String, 'unknown')
        'get'                     | [FALSE_PREDICATE, null]                           || new Reflect(null, null)
        'get'                     | [IN_INTERFACE, 'unknown']                         || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'get'                     | [IN_INTERFACE, null]                              || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'get'                     | [IN_SUPER, 'unknown']                             || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [IN_SUPER, null]                                  || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [IN_INSTANCE, 'unknown']                          || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [IN_INSTANCE, null]                               || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [TRUE_PREDICATE, 'unknown']                       || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [TRUE_PREDICATE, null]                            || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        // get
        'getInstance'             | [FIELD.name]                                      || new Reflect(FIELD.type, VALUE)
        'getNonStatic'            | [SUPER_FIELD.name]                                || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'getNonStatic'            | [FIELD.name]                                      || new Reflect(FIELD.type, VALUE)
        'getStatic'               | [INTERFACE_STATIC_FIELD.name]                     || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'getStatic'               | [SUPER_STATIC_FIELD.name]                         || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'getStatic'               | [STATIC_FIELD.name]                               || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [INTERFACE_STATIC_FIELD.name]                     || new Reflect(INTERFACE_STATIC_FIELD.type, EXPECTED_INTERFACE_STATIC_FIELD_VALUE)
        'get'                     | [SUPER_STATIC_FIELD.name]                         || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [STATIC_FIELD.name]                               || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [SUPER_FIELD.name]                                || new Reflect(SUPER_FIELD.type, SUPER_VALUE)
        'get'                     | [FIELD.name]                                      || new Reflect(FIELD.type, VALUE)
        'get'                     | [IN_SUPER]                                        || new Reflect(SUPER_STATIC_FIELD.type, EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'get'                     | [IN_INSTANCE]                                     || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        'get'                     | [TRUE_PREDICATE]                                  || new Reflect(STATIC_FIELD.type, EXPECTED_STATIC_FIELD_VALUE)
        // getField
        'getInstanceField'        | [FIELD.name]                                      || FIELD
        'getNonStaticField'       | [SUPER_FIELD.name]                                || SUPER_FIELD
        'getNonStaticField'       | [FIELD.name]                                      || FIELD
        'getStaticField'          | [INTERFACE_STATIC_FIELD.name]                     || INTERFACE_STATIC_FIELD
        'getStaticField'          | [SUPER_STATIC_FIELD.name]                         || SUPER_STATIC_FIELD
        'getStaticField'          | [STATIC_FIELD.name]                               || STATIC_FIELD
        'getField'                | [INTERFACE_STATIC_FIELD.name]                     || INTERFACE_STATIC_FIELD
        'getField'                | [SUPER_STATIC_FIELD.name]                         || SUPER_STATIC_FIELD
        'getField'                | [STATIC_FIELD.name]                               || STATIC_FIELD
        'getField'                | [SUPER_FIELD.name]                                || SUPER_FIELD
        'getField'                | [FIELD.name]                                      || FIELD
        'getField'                | [IN_SUPER]                                        || SUPER_STATIC_FIELD
        'getField'                | [IN_INSTANCE]                                     || STATIC_FIELD
        'getField'                | [TRUE_PREDICATE]                                  || STATIC_FIELD
        // getFields
        'getInstanceFields'       | []                                                || [FIELD]
        'getNonStaticFields'      | []                                                || [FIELD, SUPER_FIELD]
        'getStaticFields'         | []                                                || [STATIC_FIELD, SUPER_STATIC_FIELD, INTERFACE_STATIC_FIELD]
        'getFields'               | [FALSE_PREDICATE]                                 || []
        'getFields'               | [IN_INTERFACE]                                    || [INTERFACE_STATIC_FIELD]
        'getFields'               | [IN_SUPER]                                        || [SUPER_STATIC_FIELD, SUPER_FIELD]
        'getFields'               | [IN_INSTANCE]                                     || [STATIC_FIELD, FIELD]
        'getFields'               | [TRUE_PREDICATE]                                  || [STATIC_FIELD, FIELD, SUPER_STATIC_FIELD, SUPER_FIELD, INTERFACE_STATIC_FIELD]
        'getFields'               | []                                                || [STATIC_FIELD, FIELD, SUPER_STATIC_FIELD, SUPER_FIELD, INTERFACE_STATIC_FIELD]
        // invoke
        'invoke'                  | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(INTERFACE_VALUE.getClass(), INTERFACE_VALUE)
        'invoke'                  | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || new Reflect(INTERFACE_VALUE.getClass(), INTERFACE_VALUE)
        'invoke'                  | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(INTERFACE_VALUE.getClass(), INTERFACE_VALUE)
        'invoke'                  | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || new Reflect(INTERFACE_VALUE.getClass(), INTERFACE_VALUE)
        'invoke'                  | [SUPER_STATIC_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(EXPECTED_SUPER_STATIC_FIELD_VALUE.getClass(), EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'invoke'                  | [SUPER_STATIC_METHOD_NO_ARGS.name]                || new Reflect(EXPECTED_SUPER_STATIC_FIELD_VALUE.getClass(), EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'invoke'                  | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(EXPECTED_SUPER_STATIC_FIELD_VALUE.getClass(), EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'invoke'                  | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name]                || new Reflect(EXPECTED_SUPER_STATIC_FIELD_VALUE.getClass(), EXPECTED_SUPER_STATIC_FIELD_VALUE)
        'invoke'                  | [SUPER_STATIC_METHOD.name,
                                     [EXPECTED_SUPER_STATIC_FIELD_VALUE].toArray()]   || new Reflect(void, null)
        'invoke'                  | [SUPER_STATIC_METHOD.returnType,
                                     SUPER_STATIC_METHOD.name,
                                     [EXPECTED_SUPER_STATIC_FIELD_VALUE].toArray()]   || new Reflect(void, null)
        'invoke'                  | [SUPER_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(SUPER_VALUE.getClass(), SUPER_VALUE)
        'invoke'                  | [SUPER_METHOD_NO_ARGS.name]                       || new Reflect(SUPER_VALUE.getClass(), SUPER_VALUE)
        'invoke'                  | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(SUPER_VALUE.getClass(), SUPER_VALUE)
        'invoke'                  | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name]                       || new Reflect(SUPER_VALUE.getClass(), SUPER_VALUE)
        'invoke'                  | [[SUPER_VALUE].toArray()]                         || new Reflect(void, null)
        'invoke'                  | [SUPER_METHOD.name,
                                     [SUPER_VALUE].toArray()]                         || new Reflect(void, null)
        'invoke'                  | [SUPER_METHOD.returnType,
                                     SUPER_METHOD.name,
                                     [SUPER_VALUE].toArray()]                         || new Reflect(void, null)
        'invoke'                  | [STATIC_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(EXPECTED_STATIC_FIELD_VALUE.getClass(), EXPECTED_STATIC_FIELD_VALUE)
        'invoke'                  | [STATIC_METHOD_NO_ARGS.name]                      || new Reflect(EXPECTED_STATIC_FIELD_VALUE.getClass(), EXPECTED_STATIC_FIELD_VALUE)
        'invoke'                  | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(EXPECTED_STATIC_FIELD_VALUE.getClass(), EXPECTED_STATIC_FIELD_VALUE)
        'invoke'                  | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name]                      || new Reflect(EXPECTED_STATIC_FIELD_VALUE.getClass(), EXPECTED_STATIC_FIELD_VALUE)
        'invoke'                  | [STATIC_METHOD.name,
                                     [EXPECTED_STATIC_FIELD_VALUE].toArray()]         || new Reflect(void, null)
        'invoke'                  | [STATIC_METHOD.returnType,
                                     STATIC_METHOD.name,
                                     [EXPECTED_STATIC_FIELD_VALUE].toArray()]         || new Reflect(void, null)
        'invoke'                  | [METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(VALUE.getClass(), VALUE)
        'invoke'                  | [METHOD_NO_ARGS.name]                             || new Reflect(VALUE.getClass(), VALUE)
        'invoke'                  | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name,
                                     [].toArray()]                                    || new Reflect(VALUE.getClass(), VALUE)
        'invoke'                  | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name]                             || new Reflect(VALUE.getClass(), VALUE)
        'invoke'                  | [[VALUE].toArray()]                               || new Reflect(void, null)
        'invoke'                  | [METHOD.name,
                                     [VALUE].toArray()]                               || new Reflect(void, null)
        'invoke'                  | [METHOD.returnType,
                                     METHOD.name,
                                     [VALUE].toArray()]                               || new Reflect(void, null)
        'invoke'                  | []                                                || new Reflect(VALUE.getClass(), VALUE)
        // getMethod
        'getInstanceMethod'       | [METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getInstanceMethod'       | [METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getInstanceMethod'       | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getInstanceMethod'       | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getInstanceMethod'       | [METHOD.parameterTypes]                           || METHOD
        'getInstanceMethod'       | [METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getInstanceMethod'       | [METHOD.returnType,
                                     METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getInstanceMethod'       | []                                                || METHOD_NO_ARGS
        'getNonStaticMethod'      | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.parameterTypes] || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getNonStaticMethod'      | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getNonStaticMethod'      | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.parameterTypes] || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getNonStaticMethod'      | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getNonStaticMethod'      | [SUPER_METHOD_NO_ARGS.name,
                                     SUPER_METHOD_NO_ARGS.parameterTypes]             || SUPER_METHOD_NO_ARGS
        'getNonStaticMethod'      | [SUPER_METHOD_NO_ARGS.name]                       || SUPER_METHOD_NO_ARGS
        'getNonStaticMethod'      | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name,
                                     SUPER_METHOD_NO_ARGS.parameterTypes]             || SUPER_METHOD_NO_ARGS
        'getNonStaticMethod'      | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name]                       || SUPER_METHOD_NO_ARGS
        'getNonStaticMethod'      | [SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getNonStaticMethod'      | [SUPER_METHOD.name,
                                     SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getNonStaticMethod'      | [SUPER_METHOD.returnType,
                                     SUPER_METHOD.name,
                                     SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getNonStaticMethod'      | [METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getNonStaticMethod'      | [METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getNonStaticMethod'      | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getNonStaticMethod'      | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getNonStaticMethod'      | [METHOD.parameterTypes]                           || METHOD
        'getNonStaticMethod'      | [METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getNonStaticMethod'      | [METHOD.returnType,
                                     METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getNonStaticMethod'      | []                                                || METHOD_NO_ARGS
        'getStaticMethod'         | [SUPER_STATIC_METHOD_NO_ARGS.name,
                                     SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]      || SUPER_STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [SUPER_STATIC_METHOD_NO_ARGS.name]                || SUPER_STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name,
                                     SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]      || SUPER_STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name]                || SUPER_STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [SUPER_STATIC_METHOD.parameterTypes]              || SUPER_STATIC_METHOD
        'getStaticMethod'         | [SUPER_STATIC_METHOD.name,
                                     SUPER_STATIC_METHOD.parameterTypes]              || SUPER_STATIC_METHOD
        'getStaticMethod'         | [SUPER_STATIC_METHOD.returnType,
                                     SUPER_STATIC_METHOD.name,
                                     SUPER_STATIC_METHOD.parameterTypes]              || SUPER_STATIC_METHOD
        'getStaticMethod'         | [STATIC_METHOD_NO_ARGS.name,
                                     STATIC_METHOD_NO_ARGS.parameterTypes]            || STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [STATIC_METHOD_NO_ARGS.name]                      || STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name,
                                     STATIC_METHOD_NO_ARGS.parameterTypes]            || STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name]                      || STATIC_METHOD_NO_ARGS
        'getStaticMethod'         | [STATIC_METHOD.parameterTypes]                    || STATIC_METHOD
        'getStaticMethod'         | [STATIC_METHOD.name,
                                     STATIC_METHOD.parameterTypes]                    || STATIC_METHOD
        'getStaticMethod'         | [STATIC_METHOD.returnType,
                                     STATIC_METHOD.name,
                                     STATIC_METHOD.parameterTypes]                    || STATIC_METHOD
        'getStaticMethod'         | []                                                || STATIC_METHOD_NO_ARGS
        'getMethod'               | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.parameterTypes] || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getMethod'               | [INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getMethod'               | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.parameterTypes] || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getMethod'               | [INTERFACE_DEFAULT_METHOD_NO_ARGS.returnType,
                                     INTERFACE_DEFAULT_METHOD_NO_ARGS.name]           || INTERFACE_DEFAULT_METHOD_NO_ARGS
        'getMethod'               | [SUPER_STATIC_METHOD_NO_ARGS.name,
                                     SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]      || SUPER_STATIC_METHOD_NO_ARGS
        'getMethod'               | [SUPER_STATIC_METHOD_NO_ARGS.name]                || SUPER_STATIC_METHOD_NO_ARGS
        'getMethod'               | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name,
                                     SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]      || SUPER_STATIC_METHOD_NO_ARGS
        'getMethod'               | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                     SUPER_STATIC_METHOD_NO_ARGS.name]                || SUPER_STATIC_METHOD_NO_ARGS
        'getMethod'               | [SUPER_STATIC_METHOD.name,
                                     SUPER_STATIC_METHOD.parameterTypes]              || SUPER_STATIC_METHOD
        'getMethod'               | [SUPER_STATIC_METHOD.returnType,
                                     SUPER_STATIC_METHOD.name,
                                     SUPER_STATIC_METHOD.parameterTypes]              || SUPER_STATIC_METHOD
        'getMethod'               | [SUPER_METHOD_NO_ARGS.name,
                                     SUPER_METHOD_NO_ARGS.parameterTypes]             || SUPER_METHOD_NO_ARGS
        'getMethod'               | [SUPER_METHOD_NO_ARGS.name]                       || SUPER_METHOD_NO_ARGS
        'getMethod'               | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name,
                                     SUPER_METHOD_NO_ARGS.parameterTypes]             || SUPER_METHOD_NO_ARGS
        'getMethod'               | [SUPER_METHOD_NO_ARGS.returnType,
                                     SUPER_METHOD_NO_ARGS.name]                       || SUPER_METHOD_NO_ARGS
        'getMethod'               | [SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getMethod'               | [SUPER_METHOD.name,
                                     SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getMethod'               | [SUPER_METHOD.returnType,
                                     SUPER_METHOD.name,
                                     SUPER_METHOD.parameterTypes]                     || SUPER_METHOD
        'getMethod'               | [STATIC_METHOD_NO_ARGS.name,
                                     STATIC_METHOD_NO_ARGS.parameterTypes]            || STATIC_METHOD_NO_ARGS
        'getMethod'               | [STATIC_METHOD_NO_ARGS.name]                      || STATIC_METHOD_NO_ARGS
        'getMethod'               | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name,
                                     STATIC_METHOD_NO_ARGS.parameterTypes]            || STATIC_METHOD_NO_ARGS
        'getMethod'               | [STATIC_METHOD_NO_ARGS.returnType,
                                     STATIC_METHOD_NO_ARGS.name]                      || STATIC_METHOD_NO_ARGS
        'getMethod'               | [STATIC_METHOD.name,
                                     STATIC_METHOD.parameterTypes]                    || STATIC_METHOD
        'getMethod'               | [STATIC_METHOD.returnType,
                                     STATIC_METHOD.name,
                                     STATIC_METHOD.parameterTypes]                    || STATIC_METHOD
        'getMethod'               | [METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getMethod'               | [METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getMethod'               | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name,
                                     METHOD_NO_ARGS.parameterTypes]                   || METHOD_NO_ARGS
        'getMethod'               | [METHOD_NO_ARGS.returnType,
                                     METHOD_NO_ARGS.name]                             || METHOD_NO_ARGS
        'getMethod'               | [METHOD.parameterTypes]                           || METHOD
        'getMethod'               | [METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getMethod'               | [METHOD.returnType,
                                     METHOD.name,
                                     METHOD.parameterTypes]                           || METHOD
        'getMethod'               | [IN_SUPER]                                        || SUPER_LOMBOK_CAN_EQUAL
        'getMethod'               | [IN_INSTANCE]                                     || LOMBOK_CAN_EQUAL
        'getMethod'               | [TRUE_PREDICATE]                                  || LOMBOK_CAN_EQUAL
        'getMethod'               | []                                                || METHOD_NO_ARGS
        // getMethods
        'getInstanceMethods'      | []                                                || [*EXPECTED_METHODS]
        'getNonStaticMethods'     | []                                                ||
                [*EXPECTED_METHODS, *EXPECTED_SUPER_METHODS, *EXPECTED_INTERFACE_METHODS, *OBJECT_METHODS]
        'getStaticMethods'        | []                                                ||
                [*EXPECTED_STATIC_METHODS, *EXPECTED_SUPER_STATIC_METHODS, *OBJECT_STATIC_METHODS]
        'getMethods'              | [FALSE_PREDICATE]                                 || []
        'getMethods'              | [IN_INTERFACE]                                    || [*EXPECTED_INTERFACE_METHODS]
        'getMethods'              | [IN_SUPER]                                        ||
                [*EXPECTED_SUPER_METHODS, *EXPECTED_SUPER_STATIC_METHODS]
        'getMethods'              | [IN_INSTANCE]                                     || [*EXPECTED_METHODS, *EXPECTED_STATIC_METHODS]
        'getMethods'              | [TRUE_PREDICATE]                                  ||
                [*EXPECTED_METHODS, *EXPECTED_STATIC_METHODS,
                 *EXPECTED_SUPER_METHODS, *EXPECTED_SUPER_STATIC_METHODS,
                 *EXPECTED_INTERFACE_METHODS,
                 *OBJECT_METHODS, *OBJECT_STATIC_METHODS]
        'getMethods'              | []                                                ||
                [*EXPECTED_METHODS, *EXPECTED_STATIC_METHODS,
                 *EXPECTED_SUPER_METHODS, *EXPECTED_SUPER_STATIC_METHODS,
                 *EXPECTED_INTERFACE_METHODS,
                 *OBJECT_METHODS, *OBJECT_STATIC_METHODS]
    }

    def 'test that #method with #arguments throws ReflectException with #expected'() {
        when:
        reflect."$method"(*arguments)

        then:
        def e = thrown(ReflectException)
        e.message == expected.message

        where:
        method               | arguments                                                                 || expected
        // init
        'init'               | []                                                                        || ReflectException.cannotFindConstructor(Person, new Class[0])
        'init'               | [21, null, 'Camilla']                                                     || ReflectException.cannotFindConstructor(Person, Integer, null, String)
        // getConstructor
        'getConstructor'     | []                                                                        || ReflectException.cannotFindConstructor(Person, new Class[0])
        'getConstructor'     | [Integer, String]                                                         || ReflectException.cannotFindConstructor(Person, Integer, String)
        'getConstructor'     | [FALSE_PREDICATE]                                                         || ReflectException.cannotFindConstructor(Person)
        // get
        'getInstance'        | [SUPER_STATIC_FIELD.name]                                                 || ReflectException.cannotFindField(Person, SUPER_STATIC_FIELD.name)
        'getInstance'        | [STATIC_FIELD.name]                                                       || ReflectException.cannotFindField(Person, STATIC_FIELD.name)
        'getInstance'        | [SUPER_FIELD.name]                                                        || ReflectException.cannotFindField(Person, SUPER_FIELD.name)
        'getInstance'        | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStatic'       | [SUPER_STATIC_FIELD.name]                                                 || ReflectException.cannotFindField(Person, SUPER_STATIC_FIELD.name)
        'getNonStatic'       | [STATIC_FIELD.name]                                                       || ReflectException.cannotFindField(Person, STATIC_FIELD.name)
        'getNonStatic'       | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getStatic'          | [SUPER_FIELD.name]                                                        || ReflectException.cannotFindField(Person, SUPER_FIELD.name)
        'getStatic'          | [FIELD.name]                                                              || ReflectException.cannotFindField(Person, FIELD.name)
        'getStatic'          | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | [IN_OTHER]                                                                || ReflectException.cannotFindField(Person)
        // getField
        'getInstanceField'   | [SUPER_STATIC_FIELD.name]                                                 || ReflectException.cannotFindField(Person, SUPER_STATIC_FIELD.name)
        'getInstanceField'   | [STATIC_FIELD.name]                                                       || ReflectException.cannotFindField(Person, STATIC_FIELD.name)
        'getInstanceField'   | [SUPER_FIELD.name]                                                        || ReflectException.cannotFindField(Person, SUPER_FIELD.name)
        'getInstanceField'   | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStaticField'  | [SUPER_STATIC_FIELD.name]                                                 || ReflectException.cannotFindField(Person, SUPER_STATIC_FIELD.name)
        'getNonStaticField'  | [STATIC_FIELD.name]                                                       || ReflectException.cannotFindField(Person, STATIC_FIELD.name)
        'getNonStaticField'  | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getStaticField'     | [SUPER_FIELD.name]                                                        || ReflectException.cannotFindField(Person, SUPER_FIELD.name)
        'getStaticField'     | [FIELD.name]                                                              || ReflectException.cannotFindField(Person, FIELD.name)
        'getStaticField'     | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | [IN_OTHER]                                                                || ReflectException.cannotFindField(Person)
        'getField'           | [FALSE_PREDICATE]                                                         || ReflectException.cannotFindField(Person)
        // invoke
        'invoke'             | [['Hello, world!', null, new Object()].toArray()]                         || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'invoke'             | ['notExisting', ['Hello, world!', null, new Object()].toArray()]          || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'invoke'             | [boolean, 'notExisting', ['Hello, world!', null, new Object()].toArray()] || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        // getMethod
        'getInstanceMethod'  | [SUPER_STATIC_METHOD_NO_ARGS.name,
                                SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]                              ||
                ReflectException.cannotFindMethod(Person, null, SUPER_STATIC_METHOD_NO_ARGS.name, SUPER_STATIC_METHOD_NO_ARGS.parameterTypes)
        'getInstanceMethod'  | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                SUPER_STATIC_METHOD_NO_ARGS.name,
                                SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]                              ||
                ReflectException.cannotFindMethod(Person, SUPER_STATIC_METHOD_NO_ARGS.returnType, SUPER_STATIC_METHOD_NO_ARGS.name, SUPER_STATIC_METHOD_NO_ARGS.parameterTypes)
        'getInstanceMethod'  | [SUPER_METHOD_NO_ARGS.name,
                                SUPER_METHOD_NO_ARGS.parameterTypes]                                     ||
                ReflectException.cannotFindMethod(Person, null, SUPER_METHOD_NO_ARGS.name, SUPER_METHOD_NO_ARGS.parameterTypes)
        'getInstanceMethod'  | [SUPER_METHOD_NO_ARGS.returnType,
                                SUPER_METHOD_NO_ARGS.name,
                                SUPER_METHOD_NO_ARGS.parameterTypes]                                     ||
                ReflectException.cannotFindMethod(Person, SUPER_METHOD_NO_ARGS.returnType, SUPER_METHOD_NO_ARGS.name, SUPER_METHOD_NO_ARGS.parameterTypes)
        'getInstanceMethod'  | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getInstanceMethod'  | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getInstanceMethod'  | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [SUPER_STATIC_METHOD_NO_ARGS.name,
                                SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]                              ||
                ReflectException.cannotFindMethod(Person, null, SUPER_STATIC_METHOD_NO_ARGS.name, SUPER_STATIC_METHOD_NO_ARGS.parameterTypes)
        'getNonStaticMethod' | [SUPER_STATIC_METHOD_NO_ARGS.returnType,
                                SUPER_STATIC_METHOD_NO_ARGS.name,
                                SUPER_STATIC_METHOD_NO_ARGS.parameterTypes]                              ||
                ReflectException.cannotFindMethod(Person, SUPER_STATIC_METHOD_NO_ARGS.returnType, SUPER_STATIC_METHOD_NO_ARGS.name, SUPER_STATIC_METHOD_NO_ARGS.parameterTypes)
        'getNonStaticMethod' | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getNonStaticMethod' | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getStaticMethod'    | [SUPER_METHOD_NO_ARGS.name,
                                SUPER_METHOD_NO_ARGS.parameterTypes]                                     ||
                ReflectException.cannotFindMethod(Person, null, SUPER_METHOD_NO_ARGS.name, SUPER_METHOD_NO_ARGS.parameterTypes)
        'getStaticMethod'    | [SUPER_METHOD_NO_ARGS.returnType,
                                SUPER_METHOD_NO_ARGS.name,
                                SUPER_METHOD_NO_ARGS.parameterTypes]                                     ||
                ReflectException.cannotFindMethod(Person, SUPER_METHOD_NO_ARGS.returnType, SUPER_METHOD_NO_ARGS.name, SUPER_METHOD_NO_ARGS.parameterTypes)
        'getStaticMethod'    | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getStaticMethod'    | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getStaticMethod'    | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getMethod'          | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getMethod'          | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [FALSE_PREDICATE]                                                         || ReflectException.cannotFindMethod(Person)
        // enum
        'name'               | []                                                                        || new ReflectException('%s is not an enum', new Person(SUPER_VALUE, VALUE))
        'ordinal'            | []                                                                        || new ReflectException('%s is not an enum', new Person(SUPER_VALUE, VALUE))
        'getEnum'            | []                                                                        || new ReflectException('%s is not an enum', new Person(SUPER_VALUE, VALUE))
        'valueOf'            | ['invalid']                                                               || new ReflectException('Type \'%s\' is not an enum', Person)
        'values'             | []                                                                        || new ReflectException('Type \'%s\' is not an enum', Person)
        'getEnumClass'       | []                                                                        || new ReflectException('Type \'%s\' is not an enum', Person)
    }

    def 'test invoke of var args method'() {
        given:
        def reflect = new Reflect(String, String)

        when:
        def actual = reflect.invoke(method, *args)

        then:
        actual.get() == 'Hello, world!'

        where:
        method   | args
        'format' | ['Hello, world!']
        'format' | ['%s, world!', 'Hello']
        'format' | ['%s, %s!', 'Hello', 'world']
        'format' | ['%s, %s!', ['Hello', 'world'].toArray()]
        'join'   | [', ', 'Hello, world!']
        'join'   | [', ', 'Hello', 'world!']
    }

    def 'test that on of #arguments returns #expected'() {
        when:
        final actual = Reflect.on(*arguments)

        then:
        actual == expected

        where:
        arguments                                  || expected
        [null]                                     || new Reflect(null, null)
        [new Person('Camilla', 21)]                || new Reflect(Person, new Person('Camilla', 21))
        [Person]                                   || new Reflect(Person, Person)
        [Person.canonicalName]                     || new Reflect(Person, Person)
        [Person.canonicalName, Person.classLoader] || new Reflect(Person, Person)
    }

    def 'test that on of(Object) with type returns same as of(Type)'() {
        given:
        def type = Person

        when:
        def actual = Reflect.on((Object) type)

        then:
        actual.type == type
        actual.get() == type
    }

    def 'test that on of #arguments throws #expected on class not found'() {
        when:
        Reflect.on(*arguments)

        then:
        def e = thrown(ReflectException)
        e.message == expected.message

        where:
        arguments                            || expected
        ['not.Existing']                     || ReflectException.classNotFound('not.Existing')
        ['not.Existing', Person.classLoader] || ReflectException.classNotFound('not.Existing')
    }

    def 'test that enum method #method returns expected value for #object'() {
        given:
        def reflect = new Reflect(object instanceof Class ? object : object.getClass(), object)

        when:
        def actual = reflect."$method"(*arguments)

        then:
        actual == expected(object)

        where:
        object                | method         | arguments                                    || expected
        // name
        TimeUnit.NANOSECONDS  | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.MICROSECONDS | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.MILLISECONDS | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.SECONDS      | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.MINUTES      | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.HOURS        | 'name'         | []                                           || { it -> it.name() }
        TimeUnit.DAYS         | 'name'         | []                                           || { it -> it.name() }
        // ordinal
        TimeUnit.NANOSECONDS  | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.MICROSECONDS | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.MILLISECONDS | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.SECONDS      | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.MINUTES      | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.HOURS        | 'ordinal'      | []                                           || { it -> it.ordinal() }
        TimeUnit.DAYS         | 'ordinal'      | []                                           || { it -> it.ordinal() }
        // getEnum
        TimeUnit.NANOSECONDS  | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.MICROSECONDS | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.MILLISECONDS | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.SECONDS      | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.MINUTES      | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.HOURS        | 'getEnum'      | []                                           || { it -> it }
        TimeUnit.DAYS         | 'getEnum'      | []                                           || { it -> it }
        // valueOf
        TimeUnit              | 'valueOf'      | [TimeUnit.NANOSECONDS.name()]                || { it -> Optional.of(TimeUnit.NANOSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.NANOSECONDS.name().capitalize()]   || { it -> Optional.of(TimeUnit.NANOSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.NANOSECONDS.name().toLowerCase()]  || { it -> Optional.of(TimeUnit.NANOSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MICROSECONDS.name()]               || { it -> Optional.of(TimeUnit.MICROSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MICROSECONDS.name().capitalize()]  || { it -> Optional.of(TimeUnit.MICROSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MICROSECONDS.name().toLowerCase()] || { it -> Optional.of(TimeUnit.MICROSECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MILLISECONDS.name()]               || { it -> Optional.of(TimeUnit.MILLISECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MILLISECONDS.name().capitalize()]  || { it -> Optional.of(TimeUnit.MILLISECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MILLISECONDS.name().toLowerCase()] || { it -> Optional.of(TimeUnit.MILLISECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.SECONDS.name()]                    || { it -> Optional.of(TimeUnit.SECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.SECONDS.name().capitalize()]       || { it -> Optional.of(TimeUnit.SECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.SECONDS.name().toLowerCase()]      || { it -> Optional.of(TimeUnit.SECONDS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MINUTES.name()]                    || { it -> Optional.of(TimeUnit.MINUTES) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MINUTES.name().capitalize()]       || { it -> Optional.of(TimeUnit.MINUTES) }
        TimeUnit              | 'valueOf'      | [TimeUnit.MINUTES.name().toLowerCase()]      || { it -> Optional.of(TimeUnit.MINUTES) }
        TimeUnit              | 'valueOf'      | [TimeUnit.HOURS.name()]                      || { it -> Optional.of(TimeUnit.HOURS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.HOURS.name().capitalize()]         || { it -> Optional.of(TimeUnit.HOURS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.HOURS.name().toLowerCase()]        || { it -> Optional.of(TimeUnit.HOURS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.DAYS.name()]                       || { it -> Optional.of(TimeUnit.DAYS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.DAYS.name().capitalize()]          || { it -> Optional.of(TimeUnit.DAYS) }
        TimeUnit              | 'valueOf'      | [TimeUnit.DAYS.name().toLowerCase()]         || { it -> Optional.of(TimeUnit.DAYS) }
        TimeUnit              | 'valueOf'      | ['INVALID']                                  || { it -> Optional.empty() }
        TimeUnit              | 'valueOf'      | ['Invalid']                                  || { it -> Optional.empty() }
        TimeUnit              | 'valueOf'      | ['invalid']                                  || { it -> Optional.empty() }
        // values
        TimeUnit              | 'values'       | []                                           || { it -> TimeUnit.values().toList().toSet() }
        // getEnumClass
        TimeUnit              | 'getEnumClass' | []                                           || { it -> TimeUnit }
    }

    def 'test that cast of #type to #object returns #expected'() {
        when:
        def actual = Reflect.cast(type, object)

        then:
        actual == expected
        expected == null || actual.class == expected.class

        where:
        type      | object                  || expected
        // byte
        byte      | 1 as byte               || 1 as byte
        byte      | 1 as Byte               || 1 as byte
        byte      | 1 as short              || 1 as byte
        byte      | 1 as Short              || 1 as byte
        byte      | 1                       || 1 as byte
        byte      | 1 as Integer            || 1 as byte
        byte      | 1L                      || 1 as byte
        byte      | 1 as Long               || 1 as byte
        byte      | 1.0f                    || 1 as byte
        byte      | 1 as Float              || 1 as byte
        byte      | 1.0d                    || 1 as byte
        byte      | 1 as Double             || 1 as byte
        byte      | 'a' as char             || 97 as byte
        byte      | 'a' as Character        || 97 as byte
        // Byte
        Byte      | 1 as byte               || 1 as Byte
        Byte      | 1 as Byte               || 1 as Byte
        Byte      | 1 as short              || 1 as Byte
        Byte      | 1 as Short              || 1 as Byte
        Byte      | 1                       || 1 as Byte
        Byte      | 1 as Integer            || 1 as Byte
        Byte      | 1L                      || 1 as Byte
        Byte      | 1 as Long               || 1 as Byte
        Byte      | 1.0f                    || 1 as Byte
        Byte      | 1 as Float              || 1 as Byte
        Byte      | 1.0d                    || 1 as Byte
        Byte      | 1 as Double             || 1 as Byte
        Byte      | 'a' as char             || 97 as Byte
        Byte      | 'a' as Character        || 97 as Byte
        Byte      | null                    || null
        // char
        char      | 1 as byte               || 1 as char
        char      | 1 as Byte               || 1 as char
        char      | 1 as short              || 1 as char
        char      | 1 as Short              || 1 as char
        char      | 1                       || 1 as char
        char      | 1 as Integer            || 1 as char
        char      | 1L                      || 1 as char
        char      | 1 as Long               || 1 as char
        char      | 1.0f                    || 1 as char
        char      | 1 as Float              || 1 as char
        char      | 1.0d                    || 1 as char
        char      | 1 as Double             || 1 as char
        char      | 'a' as char             || 'a' as char
        char      | 'a' as Character        || 'a' as char
        // Character
        Character | 1 as byte               || 1 as Character
        Character | 1 as Byte               || 1 as Character
        Character | 1 as short              || 1 as Character
        Character | 1 as Short              || 1 as Character
        Character | 1                       || 1 as Character
        Character | 1 as Integer            || 1 as Character
        Character | 1L                      || 1 as Character
        Character | 1 as Long               || 1 as Character
        Character | 1.0f                    || 1 as Character
        Character | 1 as Float              || 1 as Character
        Character | 1.0d                    || 1 as Character
        Character | 1 as Double             || 1 as Character
        Character | 'a' as char             || 'a' as Character
        Character | 'a' as Character        || 'a' as Character
        Character | null                    || null
        // short
        short     | 1 as byte               || 1 as short
        short     | 1 as Byte               || 1 as short
        short     | 1 as short              || 1 as short
        short     | 1 as Short              || 1 as short
        short     | 1                       || 1 as short
        short     | 1 as Integer            || 1 as short
        short     | 1L                      || 1 as short
        short     | 1 as Long               || 1 as short
        short     | 1.0f                    || 1 as short
        short     | 1 as Float              || 1 as short
        short     | 1.0d                    || 1 as short
        short     | 1 as Double             || 1 as short
        short     | 'a' as char             || 97 as short
        short     | 'a' as Character        || 97 as short
        // Short
        Short     | 1 as byte               || 1 as Short
        Short     | 1 as Byte               || 1 as Short
        Short     | 1 as short              || 1 as Short
        Short     | 1 as Short              || 1 as Short
        Short     | 1                       || 1 as Short
        Short     | 1 as Integer            || 1 as Short
        Short     | 1L                      || 1 as Short
        Short     | 1 as Long               || 1 as Short
        Short     | 1.0f                    || 1 as Short
        Short     | 1 as Float              || 1 as Short
        Short     | 1.0d                    || 1 as Short
        Short     | 1 as Double             || 1 as Short
        Short     | 'a' as char             || 97 as Short
        Short     | 'a' as Character        || 97 as Short
        Short     | null                    || null
        // int
        int       | 1 as byte               || 1
        int       | 1 as Byte               || 1
        int       | 1 as short              || 1
        int       | 1 as Short              || 1
        int       | 1                       || 1
        int       | 1 as Integer            || 1
        int       | 1L                      || 1
        int       | 1 as Long               || 1
        int       | 1.0f                    || 1
        int       | 1 as Float              || 1
        int       | 1.0d                    || 1
        int       | 1 as Double             || 1
        int       | 'a' as char             || 97
        int       | 'a' as Character        || 97
        // Integer
        Integer   | 1 as byte               || 1
        Integer   | 1 as Byte               || 1
        Integer   | 1 as short              || 1
        Integer   | 1 as Short              || 1
        Integer   | 1                       || 1
        Integer   | 1 as Integer            || 1
        Integer   | 1L                      || 1
        Integer   | 1 as Long               || 1
        Integer   | 1.0f                    || 1
        Integer   | 1 as Float              || 1
        Integer   | 1.0d                    || 1
        Integer   | 1 as Double             || 1
        Integer   | 'a' as char             || 97
        Integer   | 'a' as Character        || 97
        Integer   | null                    || null
        // long
        long      | 1 as byte               || 1L
        long      | 1 as Byte               || 1L
        long      | 1 as short              || 1L
        long      | 1 as Short              || 1L
        long      | 1                       || 1L
        long      | 1 as Integer            || 1L
        long      | 1L                      || 1L
        long      | 1 as Long               || 1L
        long      | 1.0f                    || 1L
        long      | 1 as Float              || 1L
        long      | 1.0d                    || 1L
        long      | 1 as Double             || 1L
        long      | 'a' as char             || 97L
        long      | 'a' as Character        || 97L
        // Long
        Long      | 1 as byte               || 1L
        Long      | 1 as Byte               || 1L
        Long      | 1 as short              || 1L
        Long      | 1 as Short              || 1L
        Long      | 1                       || 1L
        Long      | 1 as Integer            || 1L
        Long      | 1L                      || 1L
        Long      | 1 as Long               || 1L
        Long      | 1.0f                    || 1L
        Long      | 1 as Float              || 1L
        Long      | 1.0d                    || 1L
        Long      | 1 as Double             || 1L
        Long      | 'a' as char             || 97L
        Long      | 'a' as Character        || 97L
        Long      | null                    || null
        // float
        float     | 1 as byte               || 1.0f
        float     | 1 as Byte               || 1.0f
        float     | 1 as short              || 1.0f
        float     | 1 as Short              || 1.0f
        float     | 1                       || 1.0f
        float     | 1 as Integer            || 1.0f
        float     | 1L                      || 1.0f
        float     | 1 as Long               || 1.0f
        float     | 1.0f                    || 1.0f
        float     | 1 as Float              || 1.0f
        float     | 1.0d                    || 1.0f
        float     | 1 as Double             || 1.0f
        float     | 'a' as char             || 97.0f
        float     | 'a' as Character        || 97.0f
        // Float
        Float     | 1 as byte               || 1.0f
        Float     | 1 as Byte               || 1.0f
        Float     | 1 as short              || 1.0f
        Float     | 1 as Short              || 1.0f
        Float     | 1                       || 1.0f
        Float     | 1 as Integer            || 1.0f
        Float     | 1L                      || 1.0f
        Float     | 1 as Long               || 1.0f
        Float     | 1.0f                    || 1.0f
        Float     | 1 as Float              || 1.0f
        Float     | 1.0d                    || 1.0f
        Float     | 1 as Double             || 1.0f
        Float     | 'a' as char             || 97.0f
        Float     | 'a' as Character        || 97.0f
        Float     | null                    || null
        // double
        double    | 1 as byte               || 1.0d
        double    | 1 as Byte               || 1.0d
        double    | 1 as short              || 1.0d
        double    | 1 as Short              || 1.0d
        double    | 1                       || 1.0d
        double    | 1 as Integer            || 1.0d
        double    | 1L                      || 1.0d
        double    | 1 as Long               || 1.0d
        double    | 1.0f                    || 1.0d
        double    | 1 as Float              || 1.0d
        double    | 1.0d                    || 1.0d
        double    | 1 as Double             || 1.0d
        double    | 'a' as char             || 97.0d
        double    | 'a' as Character        || 97.0d
        // Double
        Double    | 1 as byte               || 1.0d
        Double    | 1 as Byte               || 1.0d
        Double    | 1 as short              || 1.0d
        Double    | 1 as Short              || 1.0d
        Double    | 1                       || 1.0d
        Double    | 1 as Integer            || 1.0d
        Double    | 1L                      || 1.0d
        Double    | 1 as Long               || 1.0d
        Double    | 1.0f                    || 1.0d
        Double    | 1 as Float              || 1.0d
        Double    | 1.0d                    || 1.0d
        Double    | 1 as Double             || 1.0d
        Double    | 'a' as char             || 97.0d
        Double    | 'a' as Character        || 97.0d
        Double    | null                    || null
        // boolean
        boolean   | true                    || true
        boolean   | Boolean.TRUE as Boolean || true as Boolean
        // Boolean
        Boolean   | true                    || true
        Boolean   | Boolean.TRUE as Boolean || true as Boolean
        // String
        String    | 'Hello, world!'         || 'Hello, world!'
    }

    def 'test that cast cannot cast null to primitive #type'() {
        when:
        Reflect.cast(type, null)

        then:
        thrown(ReflectException)

        where:
        type << [byte, char, short, int, long, float, double, boolean]
    }

    def 'test that invalid cast throws'() {
        when:
        Reflect.cast(type, object)

        then:
        def e = thrown(ReflectException)
        e.message == ReflectException.cannotCast(object, type).message

        where:
        type   | object
        String | 1
        String | 'a' as char
        String | new Object()
    }

}
