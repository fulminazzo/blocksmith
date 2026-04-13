package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class ReflectTest extends Specification {
    private static final Constructor<?> constructor = Person.getDeclaredConstructor(String, Integer)

    private static final Field ENTITIES_DEFAULT_NAME = Entity.getDeclaredField('ENTITIES_DEFAULT_NAME')
    private static final Field DEFAULT_NAME = NamedEntity.getDeclaredField('DEFAULT_NAME')
    private static final Field DEFAULT_AGE = Person.getDeclaredField('DEFAULT_AGE')
    private static final Field name = NamedEntity.getDeclaredField('name')
    private static final Field age = Person.getDeclaredField('age')

    private static final Method getUniqueId = Entity.getDeclaredMethod('getUniqueId')
    private static final Method interfaceGetName = Entity.getDeclaredMethod('getName')
    private static final Method getDEFAULT_NAME = NamedEntity.getDeclaredMethod('getDEFAULT_NAME')
    private static final Method getDEFAULT_AGE = Person.getDeclaredMethod('getDEFAULT_AGE')
    private static final Method getName = NamedEntity.getDeclaredMethod('getName')
    private static final Method getAge = Person.getDeclaredMethod('getAge')

    private static final Method setDEFAULT_NAME = NamedEntity.getDeclaredMethod('setDEFAULT_NAME', String)
    private static final Method setDEFAULT_AGE = Person.getDeclaredMethod('setDEFAULT_AGE', Integer)
    private static final Method setName = NamedEntity.getDeclaredMethod('setName', String)
    private static final Method setAge = Person.getDeclaredMethod('setAge', Integer)

    private static final Method personCanEqual = Person.getDeclaredMethod('canEqual', Object)
    private static final Method personToString = Person.getDeclaredMethod('toString')
    private static final Method personEquals = Person.getDeclaredMethod('equals', Object)
    private static final Method personHashCode = Person.getDeclaredMethod('hashCode')

    private static final Method namedEntityCanEqual = NamedEntity.getDeclaredMethod('canEqual', Object)
    private static final Method namedEntityToString = NamedEntity.getDeclaredMethod('toString')
    private static final Method namedEntityEquals = NamedEntity.getDeclaredMethod('equals', Object)
    private static final Method namedEntityHashCode = NamedEntity.getDeclaredMethod('hashCode')

    private static final List<Method> objectMethods = Object.declaredMethods
            .findAll { !it.synthetic && !it.bridge }
            .sort { a, b ->
                Modifier.isStatic(a.modifiers) <=> Modifier.isStatic(b.modifiers) ?:
                        a.name <=> b.name ?: a.parameterCount <=> b.parameterCount
            }

    private static final String nameValue = 'Alex'
    private static final int ageValue = 23
    private static final UUID uuidValue = UUID.nameUUIDFromBytes(nameValue.bytes)

    static {
        DEFAULT_NAME.accessible = true
        DEFAULT_AGE.accessible = true
    }

    private Reflect reflect

    void setup() {
        reflect = new Reflect(Person, new Person(nameValue, ageValue))
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
        NamedEntity | Person      || false
        int         | Integer     || true
        int         | Number      || true
        Integer     | int         || true
        Number      | int         || false
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
        int       | 1 as int                || new Reflect(Integer, 1 as Integer)
        int       | int                     || new Reflect(Integer, Integer)
        Integer   | 1 as Integer            || new Reflect(Integer, 1 as Integer)
        Integer   | Integer                 || new Reflect(Integer, Integer)
        // long
        long      | 1 as long               || new Reflect(Long, 1 as Long)
        long      | long                    || new Reflect(Long, Long)
        Long      | 1 as Long               || new Reflect(Long, 1 as Long)
        Long      | Long                    || new Reflect(Long, Long)
        // float
        float     | 1 as float              || new Reflect(Float, 1 as Float)
        float     | float                   || new Reflect(Float, Float)
        Float     | 1 as Float              || new Reflect(Float, 1 as Float)
        Float     | Float                   || new Reflect(Float, Float)
        // double
        double    | 1 as double             || new Reflect(Double, 1 as Double)
        double    | double                  || new Reflect(Double, Double)
        Double    | 1 as Double             || new Reflect(Double, 1 as Double)
        Double    | Double                  || new Reflect(Double, Double)
        // boolean
        boolean   | true as boolean         || new Reflect(Boolean, true as Boolean)
        boolean   | boolean                 || new Reflect(Boolean, Boolean)
        Boolean   | Boolean.TRUE as Boolean || new Reflect(Boolean, true as Boolean)
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
        int       | 1 as int                || new Reflect(int, 1 as int)
        int       | int                     || new Reflect(int, int)
        Integer   | 1 as Integer            || new Reflect(int, 1 as int)
        Integer   | Integer                 || new Reflect(int, int)
        // long
        long      | 1 as long               || new Reflect(long, 1 as long)
        long      | long                    || new Reflect(long, long)
        Long      | 1 as Long               || new Reflect(long, 1 as long)
        Long      | Long                    || new Reflect(long, long)
        // float
        float     | 1 as float              || new Reflect(float, 1 as float)
        float     | float                   || new Reflect(float, float)
        Float     | 1 as Float              || new Reflect(float, 1 as float)
        Float     | Float                   || new Reflect(float, float)
        // double
        double    | 1 as double             || new Reflect(double, 1 as double)
        double    | double                  || new Reflect(double, double)
        Double    | 1 as Double             || new Reflect(double, 1 as double)
        Double    | Double                  || new Reflect(double, double)
        // boolean
        boolean   | true as boolean         || new Reflect(boolean, true as boolean)
        boolean   | boolean                 || new Reflect(boolean, boolean)
        Boolean   | Boolean.TRUE as Boolean || new Reflect(boolean, true as boolean)
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
        method                    | arguments                                                                              || expected
        // init
        'init'                    | ['Camilla', 21]                                                                        || new Reflect(Person, new Person('Camilla', 21))
        'init'                    | [[null, 21].toArray()]                                                                 || new Reflect(Person, new Person(null, 21))
        'init'                    | ['Camilla', null]                                                                      || new Reflect(Person, new Person('Camilla', null))
        'init'                    | [[null, null].toArray()]                                                               || new Reflect(Person, new Person(null, null))
        // getConstructor
        'getConstructor'          | [String, Integer]                                                                      || constructor
        'getConstructor'          | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || constructor
        'getConstructor'          | [((Predicate<Field>) (f) -> true)]                                                     || constructor
        // getConstructors
        'getConstructors'         | [((Predicate<Field>) (f) -> false)]                                                    || []
        'getConstructors'         | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || []
        'getConstructors'         | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || [constructor]
        'getConstructors'         | [((Predicate<Field>) (f) -> true)]                                                     || [constructor]
        'getConstructors'         | []                                                                                     || [constructor]
        // getFieldValues
        'getInstanceFieldValues'  | []                                                                                     || [ageValue].collect { new Reflect(it.class, it) }
        'getNonStaticFieldValues' | []                                                                                     || [ageValue, nameValue].collect { new Reflect(it.class, it) }
        'getStaticFieldValues'    | []                                                                                     || [DEFAULT_AGE.get(null), DEFAULT_NAME.get(null), ENTITIES_DEFAULT_NAME.get(null)].collect { new Reflect(it.class, it) }
        'getFieldValues'          | [((Predicate<Field>) (f) -> false)]                                                    || [].collect { new Reflect(it.class, it) }
        'getFieldValues'          | [((Predicate<Field>) (f) -> f.declaringClass == Entity)]                               || [ENTITIES_DEFAULT_NAME.get(null)].collect { new Reflect(it.class, it) }
        'getFieldValues'          | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || [DEFAULT_NAME.get(null), nameValue].collect { new Reflect(it.class, it) }
        'getFieldValues'          | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || [DEFAULT_AGE.get(null), ageValue].collect { new Reflect(it.class, it) }
        'getFieldValues'          | [((Predicate<Field>) (f) -> true)]                                                     || [DEFAULT_AGE.get(null), ageValue, DEFAULT_NAME.get(null), nameValue, ENTITIES_DEFAULT_NAME.get(null)].collect { new Reflect(it.class, it) }
        'getFieldValues'          | []                                                                                     || [DEFAULT_AGE.get(null), ageValue, DEFAULT_NAME.get(null), nameValue, ENTITIES_DEFAULT_NAME.get(null)].collect { new Reflect(it.class, it) }
        // set
        'setInstance'             | [age.name, ageValue]                                                                   || new Reflect(Person, new Person(nameValue, ageValue))
        'setNonStatic'            | [name.name, nameValue]                                                                 || new Reflect(Person, new Person(nameValue, ageValue))
        'setNonStatic'            | [age.name, ageValue]                                                                   || new Reflect(Person, new Person(nameValue, ageValue))
        'setStatic'               | [DEFAULT_NAME.name, DEFAULT_NAME.get(null)]                                            || new Reflect(Person, new Person(nameValue, ageValue))
        'setStatic'               | [DEFAULT_AGE.name, DEFAULT_AGE.get(null)]                                              || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [DEFAULT_NAME.name, DEFAULT_NAME.get(null)]                                            || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [DEFAULT_AGE.name, DEFAULT_AGE.get(null)]                                              || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [name.name, nameValue]                                                                 || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [age.name, ageValue]                                                                   || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity), DEFAULT_NAME.get(null)]  || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [((Predicate<Field>) (f) -> f.declaringClass == Person), DEFAULT_AGE.get(null)]        || new Reflect(Person, new Person(nameValue, ageValue))
        'set'                     | [((Predicate<Field>) (f) -> true), DEFAULT_AGE.get(null)]                              || new Reflect(Person, new Person(nameValue, ageValue))
        // get orElse
        'getInstance'             | [DEFAULT_NAME.name, 'unknown']                                                         || new Reflect(String, 'unknown')
        'getInstance'             | [DEFAULT_NAME.name, null]                                                              || new Reflect(null, null)
        'getInstance'             | [DEFAULT_AGE.name, 15]                                                                 || new Reflect(Integer, 15)
        'getInstance'             | [DEFAULT_AGE.name, null]                                                               || new Reflect(null, null)
        'getInstance'             | [name.name, 'unknown']                                                                 || new Reflect(String, 'unknown')
        'getInstance'             | [name.name, null]                                                                      || new Reflect(null, null)
        'getInstance'             | [age.name, 15]                                                                         || new Reflect(age.type, ageValue)
        'getInstance'             | [age.name, null]                                                                       || new Reflect(age.type, ageValue)
        'getNonStatic'            | [DEFAULT_NAME.name, 'unknown']                                                         || new Reflect(String, 'unknown')
        'getNonStatic'            | [DEFAULT_NAME.name, null]                                                              || new Reflect(null, null)
        'getNonStatic'            | [DEFAULT_AGE.name, 15]                                                                 || new Reflect(Integer, 15)
        'getNonStatic'            | [DEFAULT_AGE.name, null]                                                               || new Reflect(null, null)
        'getNonStatic'            | [name.name, 'unknown']                                                                 || new Reflect(name.type, nameValue)
        'getNonStatic'            | [name.name, null]                                                                      || new Reflect(name.type, nameValue)
        'getNonStatic'            | [age.name, 15]                                                                         || new Reflect(age.type, ageValue)
        'getNonStatic'            | [age.name, null]                                                                       || new Reflect(age.type, ageValue)
        'getStatic'               | [DEFAULT_NAME.name, 'unknown']                                                         || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getStatic'               | [DEFAULT_NAME.name, null]                                                              || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getStatic'               | [ENTITIES_DEFAULT_NAME.name, 'unknown']                                                || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'getStatic'               | [ENTITIES_DEFAULT_NAME.name, null]                                                     || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'getStatic'               | [DEFAULT_AGE.name, 15]                                                                 || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'getStatic'               | [DEFAULT_AGE.name, null]                                                               || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'getStatic'               | [name.name, 'unknown']                                                                 || new Reflect(String, 'unknown')
        'getStatic'               | [name.name, null]                                                                      || new Reflect(null, null)
        'getStatic'               | [age.name, 15]                                                                         || new Reflect(Integer, 15)
        'getStatic'               | [age.name, null]                                                                       || new Reflect(null, null)
        'get'                     | [ENTITIES_DEFAULT_NAME.name, 'unknown']                                                || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'get'                     | [ENTITIES_DEFAULT_NAME.name, null]                                                     || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'get'                     | [DEFAULT_NAME.name, 'unknown']                                                         || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [DEFAULT_NAME.name, null]                                                              || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [DEFAULT_AGE.name, 15]                                                                 || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [DEFAULT_AGE.name, null]                                                               || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [name.name, 'unknown']                                                                 || new Reflect(name.type, nameValue)
        'get'                     | [name.name, null]                                                                      || new Reflect(name.type, nameValue)
        'get'                     | [age.name, 15]                                                                         || new Reflect(age.type, ageValue)
        'get'                     | [age.name, null]                                                                       || new Reflect(age.type, ageValue)
        'get'                     | ['unknown', 'unknown']                                                                 || new Reflect(String, 'unknown')
        'get'                     | ['unknown', null]                                                                      || new Reflect(null, null)
        'get'                     | [((Predicate<Field>) (f) -> false), 'unknown']                                         || new Reflect(String, 'unknown')
        'get'                     | [((Predicate<Field>) (f) -> false), null]                                              || new Reflect(null, null)
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == Entity), 'unknown']                    || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == Entity), null]                         || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity), 'unknown']               || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity), null]                    || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == Person), 'unknown']                    || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == Person), null]                         || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [((Predicate<Field>) (f) -> true), 'unknown']                                          || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [((Predicate<Field>) (f) -> true), null]                                               || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        // get
        'getInstance'             | [age.name]                                                                             || new Reflect(age.type, ageValue)
        'getNonStatic'            | [name.name]                                                                            || new Reflect(name.type, nameValue)
        'getNonStatic'            | [age.name]                                                                             || new Reflect(age.type, ageValue)
        'getStatic'               | [ENTITIES_DEFAULT_NAME.name]                                                           || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'getStatic'               | [DEFAULT_NAME.name]                                                                    || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'getStatic'               | [DEFAULT_AGE.name]                                                                     || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [ENTITIES_DEFAULT_NAME.name]                                                           || new Reflect(ENTITIES_DEFAULT_NAME.type, ENTITIES_DEFAULT_NAME.get(null))
        'get'                     | [DEFAULT_NAME.name]                                                                    || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [DEFAULT_AGE.name]                                                                     || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [name.name]                                                                            || new Reflect(name.type, nameValue)
        'get'                     | [age.name]                                                                             || new Reflect(age.type, ageValue)
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || new Reflect(DEFAULT_NAME.type, DEFAULT_NAME.get(null))
        'get'                     | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        'get'                     | [((Predicate<Field>) (f) -> true)]                                                     || new Reflect(DEFAULT_AGE.type, DEFAULT_AGE.get(null))
        // getField
        'getInstanceField'        | [age.name]                                                                             || age
        'getNonStaticField'       | [name.name]                                                                            || name
        'getNonStaticField'       | [age.name]                                                                             || age
        'getStaticField'          | [ENTITIES_DEFAULT_NAME.name]                                                           || ENTITIES_DEFAULT_NAME
        'getStaticField'          | [DEFAULT_NAME.name]                                                                    || DEFAULT_NAME
        'getStaticField'          | [DEFAULT_AGE.name]                                                                     || DEFAULT_AGE
        'getField'                | [ENTITIES_DEFAULT_NAME.name]                                                           || ENTITIES_DEFAULT_NAME
        'getField'                | [DEFAULT_NAME.name]                                                                    || DEFAULT_NAME
        'getField'                | [DEFAULT_AGE.name]                                                                     || DEFAULT_AGE
        'getField'                | [name.name]                                                                            || name
        'getField'                | [age.name]                                                                             || age
        'getField'                | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || DEFAULT_NAME
        'getField'                | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || DEFAULT_AGE
        'getField'                | [((Predicate<Field>) (f) -> true)]                                                     || DEFAULT_AGE
        // getFields
        'getInstanceFields'       | []                                                                                     || [age]
        'getNonStaticFields'      | []                                                                                     || [age, name]
        'getStaticFields'         | []                                                                                     || [DEFAULT_AGE, DEFAULT_NAME, ENTITIES_DEFAULT_NAME]
        'getFields'               | [((Predicate<Field>) (f) -> false)]                                                    || []
        'getFields'               | [((Predicate<Field>) (f) -> f.declaringClass == Entity)]                               || [ENTITIES_DEFAULT_NAME]
        'getFields'               | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || [DEFAULT_NAME, name]
        'getFields'               | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || [DEFAULT_AGE, age]
        'getFields'               | [((Predicate<Field>) (f) -> true)]                                                     || [DEFAULT_AGE, age, DEFAULT_NAME, name, ENTITIES_DEFAULT_NAME]
        'getFields'               | []                                                                                     || [DEFAULT_AGE, age, DEFAULT_NAME, name, ENTITIES_DEFAULT_NAME]
        // invoke
        'invoke'                  | [getUniqueId.name, [].toArray()]                                                       || new Reflect(uuidValue.class, uuidValue)
        'invoke'                  | [getUniqueId.name]                                                                     || new Reflect(uuidValue.class, uuidValue)
        'invoke'                  | [getUniqueId.returnType, getUniqueId.name, [].toArray()]                               || new Reflect(uuidValue.class, uuidValue)
        'invoke'                  | [getUniqueId.returnType, getUniqueId.name]                                             || new Reflect(uuidValue.class, uuidValue)
        'invoke'                  | [getDEFAULT_NAME.name, [].toArray()]                                                   || new Reflect(DEFAULT_NAME.get(null).class, DEFAULT_NAME.get(null))
        'invoke'                  | [getDEFAULT_NAME.name]                                                                 || new Reflect(DEFAULT_NAME.get(null).class, DEFAULT_NAME.get(null))
        'invoke'                  | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, [].toArray()]                       || new Reflect(DEFAULT_NAME.get(null).class, DEFAULT_NAME.get(null))
        'invoke'                  | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name]                                     || new Reflect(DEFAULT_NAME.get(null).class, DEFAULT_NAME.get(null))
        'invoke'                  | [setDEFAULT_NAME.name, [DEFAULT_NAME.get(null)].toArray()]                             || new Reflect(void, null)
        'invoke'                  | [setDEFAULT_NAME.returnType, setDEFAULT_NAME.name, [DEFAULT_NAME.get(null)].toArray()] || new Reflect(void, null)
        'invoke'                  | [getName.name, [].toArray()]                                                           || new Reflect(nameValue.class, nameValue)
        'invoke'                  | [getName.name]                                                                         || new Reflect(nameValue.class, nameValue)
        'invoke'                  | [getName.returnType, getName.name, [].toArray()]                                       || new Reflect(nameValue.class, nameValue)
        'invoke'                  | [getName.returnType, getName.name]                                                     || new Reflect(nameValue.class, nameValue)
        'invoke'                  | [[nameValue].toArray()]                                                                || new Reflect(void, null)
        'invoke'                  | [setName.name, [nameValue].toArray()]                                                  || new Reflect(void, null)
        'invoke'                  | [setName.returnType, setName.name, [nameValue].toArray()]                              || new Reflect(void, null)
        'invoke'                  | [getDEFAULT_AGE.name, [].toArray()]                                                    || new Reflect(DEFAULT_AGE.get(null).class, DEFAULT_AGE.get(null))
        'invoke'                  | [getDEFAULT_AGE.name]                                                                  || new Reflect(DEFAULT_AGE.get(null).class, DEFAULT_AGE.get(null))
        'invoke'                  | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name, [].toArray()]                         || new Reflect(DEFAULT_AGE.get(null).class, DEFAULT_AGE.get(null))
        'invoke'                  | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name]                                       || new Reflect(DEFAULT_AGE.get(null).class, DEFAULT_AGE.get(null))
        'invoke'                  | [setDEFAULT_AGE.name, [DEFAULT_AGE.get(null)].toArray()]                               || new Reflect(void, null)
        'invoke'                  | [setDEFAULT_AGE.returnType, setDEFAULT_AGE.name, [DEFAULT_AGE.get(null)].toArray()]    || new Reflect(void, null)
        'invoke'                  | [getAge.name, [].toArray()]                                                            || new Reflect(ageValue.class, ageValue)
        'invoke'                  | [getAge.name]                                                                          || new Reflect(ageValue.class, ageValue)
        'invoke'                  | [getAge.returnType, getAge.name, [].toArray()]                                         || new Reflect(ageValue.class, ageValue)
        'invoke'                  | [getAge.returnType, getAge.name]                                                       || new Reflect(ageValue.class, ageValue)
        'invoke'                  | [[ageValue].toArray()]                                                                 || new Reflect(void, null)
        'invoke'                  | [setAge.name, [ageValue].toArray()]                                                    || new Reflect(void, null)
        'invoke'                  | [setAge.returnType, setAge.name, [ageValue].toArray()]                                 || new Reflect(void, null)
        'invoke'                  | []                                                                                     || new Reflect(ageValue.class, ageValue)
        // getMethod
        'getInstanceMethod'       | [getAge.name, getAge.parameterTypes]                                                   || getAge
        'getInstanceMethod'       | [getAge.name]                                                                          || getAge
        'getInstanceMethod'       | [getAge.returnType, getAge.name, getAge.parameterTypes]                                || getAge
        'getInstanceMethod'       | [getAge.returnType, getAge.name]                                                       || getAge
        'getInstanceMethod'       | [setAge.parameterTypes]                                                                || setAge
        'getInstanceMethod'       | [setAge.name, setAge.parameterTypes]                                                   || setAge
        'getInstanceMethod'       | [setAge.returnType, setAge.name, setAge.parameterTypes]                                || setAge
        'getInstanceMethod'       | []                                                                                     || getAge
        'getNonStaticMethod'      | [getUniqueId.name, getUniqueId.parameterTypes]                                         || getUniqueId
        'getNonStaticMethod'      | [getUniqueId.name]                                                                     || getUniqueId
        'getNonStaticMethod'      | [getUniqueId.returnType, getUniqueId.name, getUniqueId.parameterTypes]                 || getUniqueId
        'getNonStaticMethod'      | [getUniqueId.returnType, getUniqueId.name]                                             || getUniqueId
        'getNonStaticMethod'      | [getName.name, getName.parameterTypes]                                                 || getName
        'getNonStaticMethod'      | [getName.name]                                                                         || getName
        'getNonStaticMethod'      | [getName.returnType, getName.name, getName.parameterTypes]                             || getName
        'getNonStaticMethod'      | [getName.returnType, getName.name]                                                     || getName
        'getNonStaticMethod'      | [setName.parameterTypes]                                                               || setName
        'getNonStaticMethod'      | [setName.name, setName.parameterTypes]                                                 || setName
        'getNonStaticMethod'      | [setName.returnType, setName.name, setName.parameterTypes]                             || setName
        'getNonStaticMethod'      | [getAge.name, getAge.parameterTypes]                                                   || getAge
        'getNonStaticMethod'      | [getAge.name]                                                                          || getAge
        'getNonStaticMethod'      | [getAge.returnType, getAge.name, getAge.parameterTypes]                                || getAge
        'getNonStaticMethod'      | [getAge.returnType, getAge.name]                                                       || getAge
        'getNonStaticMethod'      | [setAge.parameterTypes]                                                                || setAge
        'getNonStaticMethod'      | [setAge.name, setAge.parameterTypes]                                                   || setAge
        'getNonStaticMethod'      | [setAge.returnType, setAge.name, setAge.parameterTypes]                                || setAge
        'getNonStaticMethod'      | []                                                                                     || getAge
        'getStaticMethod'         | [getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]                                 || getDEFAULT_NAME
        'getStaticMethod'         | [getDEFAULT_NAME.name]                                                                 || getDEFAULT_NAME
        'getStaticMethod'         | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]     || getDEFAULT_NAME
        'getStaticMethod'         | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name]                                     || getDEFAULT_NAME
        'getStaticMethod'         | [setDEFAULT_NAME.parameterTypes]                                                       || setDEFAULT_NAME
        'getStaticMethod'         | [setDEFAULT_NAME.name, setDEFAULT_NAME.parameterTypes]                                 || setDEFAULT_NAME
        'getStaticMethod'         | [setDEFAULT_NAME.returnType, setDEFAULT_NAME.name, setDEFAULT_NAME.parameterTypes]     || setDEFAULT_NAME
        'getStaticMethod'         | [getDEFAULT_AGE.name, getDEFAULT_AGE.parameterTypes]                                   || getDEFAULT_AGE
        'getStaticMethod'         | [getDEFAULT_AGE.name]                                                                  || getDEFAULT_AGE
        'getStaticMethod'         | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name, getDEFAULT_AGE.parameterTypes]        || getDEFAULT_AGE
        'getStaticMethod'         | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name]                                       || getDEFAULT_AGE
        'getStaticMethod'         | [setDEFAULT_AGE.parameterTypes]                                                        || setDEFAULT_AGE
        'getStaticMethod'         | [setDEFAULT_AGE.name, setDEFAULT_AGE.parameterTypes]                                   || setDEFAULT_AGE
        'getStaticMethod'         | [setDEFAULT_AGE.returnType, setDEFAULT_AGE.name, setDEFAULT_AGE.parameterTypes]        || setDEFAULT_AGE
        'getStaticMethod'         | []                                                                                     || getDEFAULT_AGE
        'getMethod'               | [getUniqueId.name, getUniqueId.parameterTypes]                                         || getUniqueId
        'getMethod'               | [getUniqueId.name]                                                                     || getUniqueId
        'getMethod'               | [getUniqueId.returnType, getUniqueId.name, getUniqueId.parameterTypes]                 || getUniqueId
        'getMethod'               | [getUniqueId.returnType, getUniqueId.name]                                             || getUniqueId
        'getMethod'               | [getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]                                 || getDEFAULT_NAME
        'getMethod'               | [getDEFAULT_NAME.name]                                                                 || getDEFAULT_NAME
        'getMethod'               | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]     || getDEFAULT_NAME
        'getMethod'               | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name]                                     || getDEFAULT_NAME
        'getMethod'               | [setDEFAULT_NAME.name, setDEFAULT_NAME.parameterTypes]                                 || setDEFAULT_NAME
        'getMethod'               | [setDEFAULT_NAME.returnType, setDEFAULT_NAME.name, setDEFAULT_NAME.parameterTypes]     || setDEFAULT_NAME
        'getMethod'               | [getName.name, getName.parameterTypes]                                                 || getName
        'getMethod'               | [getName.name]                                                                         || getName
        'getMethod'               | [getName.returnType, getName.name, getName.parameterTypes]                             || getName
        'getMethod'               | [getName.returnType, getName.name]                                                     || getName
        'getMethod'               | [setName.parameterTypes]                                                               || setName
        'getMethod'               | [setName.name, setName.parameterTypes]                                                 || setName
        'getMethod'               | [setName.returnType, setName.name, setName.parameterTypes]                             || setName
        'getMethod'               | [getDEFAULT_AGE.name, getDEFAULT_AGE.parameterTypes]                                   || getDEFAULT_AGE
        'getMethod'               | [getDEFAULT_AGE.name]                                                                  || getDEFAULT_AGE
        'getMethod'               | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name, getDEFAULT_AGE.parameterTypes]        || getDEFAULT_AGE
        'getMethod'               | [getDEFAULT_AGE.returnType, getDEFAULT_AGE.name]                                       || getDEFAULT_AGE
        'getMethod'               | [setDEFAULT_AGE.name, setDEFAULT_AGE.parameterTypes]                                   || setDEFAULT_AGE
        'getMethod'               | [setDEFAULT_AGE.returnType, setDEFAULT_AGE.name, setDEFAULT_AGE.parameterTypes]        || setDEFAULT_AGE
        'getMethod'               | [getAge.name, getAge.parameterTypes]                                                   || getAge
        'getMethod'               | [getAge.name]                                                                          || getAge
        'getMethod'               | [getAge.returnType, getAge.name, getAge.parameterTypes]                                || getAge
        'getMethod'               | [getAge.returnType, getAge.name]                                                       || getAge
        'getMethod'               | [setAge.parameterTypes]                                                                || setAge
        'getMethod'               | [setAge.name, setAge.parameterTypes]                                                   || setAge
        'getMethod'               | [setAge.returnType, setAge.name, setAge.parameterTypes]                                || setAge
        'getMethod'               | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          || namedEntityCanEqual
        'getMethod'               | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               || personCanEqual
        'getMethod'               | [((Predicate<Field>) (f) -> true)]                                                     || personCanEqual
        'getMethod'               | []                                                                                     || getAge
        // getMethods
        'getInstanceMethods'      | []                                                                                     ||
                [personCanEqual, personEquals, getAge, personHashCode, setAge, personToString]
        'getNonStaticMethods'     | []                                                                                     ||
                [personCanEqual, personEquals, getAge, personHashCode, setAge, personToString,
                 namedEntityCanEqual, namedEntityEquals, getName, namedEntityHashCode, setName, namedEntityToString,
                 interfaceGetName, getUniqueId,
                 *objectMethods.findAll { !Modifier.isStatic(it.modifiers) }]
        'getStaticMethods'        | []                                                                                     ||
                [getDEFAULT_AGE, setDEFAULT_AGE, getDEFAULT_NAME, setDEFAULT_NAME, *objectMethods.findAll { Modifier.isStatic(it.modifiers) }]
        'getMethods'              | [((Predicate<Field>) (f) -> false)]                                                    || []
        'getMethods'              | [((Predicate<Field>) (f) -> f.declaringClass == Entity)]                               ||
                [interfaceGetName, getUniqueId]
        'getMethods'              | [((Predicate<Field>) (f) -> f.declaringClass == NamedEntity)]                          ||
                [namedEntityCanEqual, namedEntityEquals, getName, namedEntityHashCode, setName, namedEntityToString, getDEFAULT_NAME, setDEFAULT_NAME]
        'getMethods'              | [((Predicate<Field>) (f) -> f.declaringClass == Person)]                               ||
                [personCanEqual, personEquals, getAge, personHashCode, setAge, personToString, getDEFAULT_AGE, setDEFAULT_AGE]
        'getMethods'              | [((Predicate<Field>) (f) -> true)]                                                     ||
                [personCanEqual, personEquals, getAge, personHashCode, setAge, personToString, getDEFAULT_AGE, setDEFAULT_AGE,
                 namedEntityCanEqual, namedEntityEquals, getName, namedEntityHashCode, setName, namedEntityToString, getDEFAULT_NAME, setDEFAULT_NAME,
                 interfaceGetName, getUniqueId,
                 *objectMethods]
        'getMethods'              | []                                                                                     ||
                [personCanEqual, personEquals, getAge, personHashCode, setAge, personToString, getDEFAULT_AGE, setDEFAULT_AGE,
                 namedEntityCanEqual, namedEntityEquals, getName, namedEntityHashCode, setName, namedEntityToString, getDEFAULT_NAME, setDEFAULT_NAME,
                 interfaceGetName, getUniqueId,
                 *objectMethods]
    }

    def 'test that #method with #arguments throws ReflectException with #expected'() {
        when:
        reflect."$method"(*arguments)

        then:
        def e = thrown(ReflectException)
        e.message == expected.message

        where:
        method               | arguments                                                                          || expected
        // init
        'init'               | []                                                                                 || ReflectException.cannotFindConstructor(Person, new Class[0])
        'init'               | [21, null, 'Camilla']                                                              || ReflectException.cannotFindConstructor(Person, Integer, null, String)
        // getConstructor
        'getConstructor'     | []                                                                                 || ReflectException.cannotFindConstructor(Person, new Class[0])
        'getConstructor'     | [Integer, String]                                                                  || ReflectException.cannotFindConstructor(Person, Integer, String)
        'getConstructor'     | [((Predicate<Field>) (f) -> false)]                                                || ReflectException.cannotFindConstructor(Person)
        // get
        'getInstance'        | [DEFAULT_NAME.name]                                                                || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getInstance'        | [DEFAULT_AGE.name]                                                                 || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getInstance'        | [name.name]                                                                        || ReflectException.cannotFindField(Person, name.name)
        'getInstance'        | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStatic'       | [DEFAULT_NAME.name]                                                                || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getNonStatic'       | [DEFAULT_AGE.name]                                                                 || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getNonStatic'       | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getStatic'          | [name.name]                                                                        || ReflectException.cannotFindField(Person, name.name)
        'getStatic'          | [age.name]                                                                         || ReflectException.cannotFindField(Person, age.name)
        'getStatic'          | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | [((Predicate<Field>) (f) -> f.declaringClass == String)]                           || ReflectException.cannotFindField(Person)
        // getField
        'getInstanceField'   | [DEFAULT_NAME.name]                                                                || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getInstanceField'   | [DEFAULT_AGE.name]                                                                 || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getInstanceField'   | [name.name]                                                                        || ReflectException.cannotFindField(Person, name.name)
        'getInstanceField'   | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStaticField'  | [DEFAULT_NAME.name]                                                                || ReflectException.cannotFindField(Person, DEFAULT_NAME.name)
        'getNonStaticField'  | [DEFAULT_AGE.name]                                                                 || ReflectException.cannotFindField(Person, DEFAULT_AGE.name)
        'getNonStaticField'  | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getStaticField'     | [name.name]                                                                        || ReflectException.cannotFindField(Person, name.name)
        'getStaticField'     | [age.name]                                                                         || ReflectException.cannotFindField(Person, age.name)
        'getStaticField'     | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                                    || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | [((Predicate<Field>) (f) -> f.declaringClass == String)]                           || ReflectException.cannotFindField(Person)
        'getField'           | [((Predicate<Field>) (f) -> false)]                                                || ReflectException.cannotFindField(Person)
        // invoke
        'invoke'             | [['Hello, world!', null, new Object()].toArray()]                                  || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'invoke'             | ['notExisting', ['Hello, world!', null, new Object()].toArray()]                   || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'invoke'             | [boolean, 'notExisting', ['Hello, world!', null, new Object()].toArray()]          || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        // getMethod
        'getInstanceMethod'  | [getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]                             || ReflectException.cannotFindMethod(Person, null, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes)
        'getInstanceMethod'  | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes] || ReflectException.cannotFindMethod(Person, getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes)
        'getInstanceMethod'  | [getName.name, getName.parameterTypes]                                             || ReflectException.cannotFindMethod(Person, null, getName.name, getName.parameterTypes)
        'getInstanceMethod'  | [getName.returnType, getName.name, getName.parameterTypes]                         || ReflectException.cannotFindMethod(Person, getName.returnType, getName.name, getName.parameterTypes)
        'getInstanceMethod'  | [[String, null, Object].toArray(new Class[3])]                                     || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getInstanceMethod'  | ['notExisting', [String, null, Object].toArray(new Class[3])]                      || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getInstanceMethod'  | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes]                             || ReflectException.cannotFindMethod(Person, null, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes)
        'getNonStaticMethod' | [getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes] || ReflectException.cannotFindMethod(Person, getDEFAULT_NAME.returnType, getDEFAULT_NAME.name, getDEFAULT_NAME.parameterTypes)
        'getNonStaticMethod' | [[String, null, Object].toArray(new Class[3])]                                     || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getNonStaticMethod' | ['notExisting', [String, null, Object].toArray(new Class[3])]                      || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getStaticMethod'    | [getName.name, getName.parameterTypes]                                             || ReflectException.cannotFindMethod(Person, null, getName.name, getName.parameterTypes)
        'getStaticMethod'    | [getName.returnType, getName.name, getName.parameterTypes]                         || ReflectException.cannotFindMethod(Person, getName.returnType, getName.name, getName.parameterTypes)
        'getStaticMethod'    | [[String, null, Object].toArray(new Class[3])]                                     || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getStaticMethod'    | ['notExisting', [String, null, Object].toArray(new Class[3])]                      || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getStaticMethod'    | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [[String, null, Object].toArray(new Class[3])]                                     || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getMethod'          | ['notExisting', [String, null, Object].toArray(new Class[3])]                      || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getMethod'          | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [((Predicate<Field>) (f) -> false)]                                                || ReflectException.cannotFindMethod(Person)
        // enum
        'name'               | []                                                                                 || new ReflectException('%s is not an enum', new Person(nameValue, ageValue))
        'ordinal'            | []                                                                                 || new ReflectException('%s is not an enum', new Person(nameValue, ageValue))
        'getEnum'            | []                                                                                 || new ReflectException('%s is not an enum', new Person(nameValue, ageValue))
        'valueOf'            | ['invalid']                                                                        || new ReflectException('Type \'%s\' is not an enum', Person)
        'values'             | []                                                                                 || new ReflectException('Type \'%s\' is not an enum', Person)
        'getEnumClass'       | []                                                                                 || new ReflectException('Type \'%s\' is not an enum', Person)
    }

    def 'test that init with #exception throws #expected'() {
        given:
        def constructor = Mock(Constructor)
        constructor.newInstance(_) >> {
            throw exception
        }
        constructor.declaringClass >> Person
        constructor.parameters >> [].toArray()
        constructor.parameterTypes >> [].toArray()

        when:
        reflect.init(constructor)

        then:
        thrown(expected)

        where:
        exception                                                                     || expected
        new InvocationTargetException(new RuntimeException('Test runtime exception')) || ReflectException
        new InvocationTargetException(new Error('Test error'))                        || ReflectException
        new InvocationTargetException(new Exception('Test exception'))                || ReflectException
    }

    def 'test that init throws ReflectException on IllegalAccessException'() {
        given:
        def constructor = Mock(Constructor)
        constructor.newInstance(_) >> {
            throw new IllegalAccessException()
        }
        constructor.declaringClass >> Person
        constructor.parameters >> [].toArray()
        constructor.parameterTypes >> [].toArray()

        when:
        reflect.init(constructor)

        then:
        thrown(ReflectException)
    }

    def 'test that get of not accessible field does not throw'() {
        given:
        def field = Mock(Field)
        field.get(_) >> {
            throw new IllegalAccessException()
        }
        field.genericType >> String
        field.type >> String

        when:
        def actual = reflect.get(field, 'Hello, world!')

        then:
        actual == new Reflect(String, 'Hello, world!')
    }

    def 'test that get throws ReflectException on IllegalAccessException'() {
        given:
        def field = Mock(Field)
        field.get(_) >> {
            throw new IllegalAccessException()
        }

        when:
        reflect.get(field)

        then:
        thrown(ReflectException)
    }

    def 'test that set throws ReflectException on IllegalAccessException'() {
        given:
        def field = Mock(Field)
        field.set(_, _) >> {
            throw new IllegalAccessException()
        }

        when:
        reflect.set(field, null)

        then:
        thrown(ReflectException)
    }

    def 'test that invoke with #exception throws #expected'() {
        given:
        def method = Mock(Method)
        method.invoke(_, _) >> {
            throw exception
        }
        method.parameters >> [].toArray()
        method.parameterTypes >> [].toArray()
        method.returnType >> void

        when:
        reflect.invoke(method)

        then:
        thrown(expected)

        where:
        exception                                                                     || expected
        new InvocationTargetException(new RuntimeException('Test runtime exception')) || ReflectException
        new InvocationTargetException(new Error('Test error'))                        || ReflectException
        new InvocationTargetException(new Exception('Test exception'))                || ReflectException
    }

    def 'test that invoke throws ReflectException on IllegalAccessException'() {
        given:
        def method = Mock(Method)
        method.invoke(_, _) >> {
            throw new IllegalAccessException()
        }
        method.parameters >> [].toArray()
        method.parameterTypes >> [].toArray()
        method.returnType >> void

        when:
        reflect.invoke(method)

        then:
        thrown(ReflectException)
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
        def reflect = new Reflect(object instanceof Class ? object : object.class, object)

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
        Byte      | 1 as byte               || 1 as Byte
        Byte      | 1 as Byte               || 1 as Byte
        Byte      | 1 as short              || 1 as Byte
        Byte      | 1 as Short              || 1 as Byte
        Byte      | 1 as int                || 1 as Byte
        Byte      | 1 as Integer            || 1 as Byte
        Byte      | 1 as long               || 1 as Byte
        Byte      | 1 as Long               || 1 as Byte
        Byte      | 1 as float              || 1 as Byte
        Byte      | 1 as Float              || 1 as Byte
        Byte      | 1 as double             || 1 as Byte
        Byte      | 1 as Double             || 1 as Byte
        Byte      | 'a' as char             || 97 as Byte
        Byte      | 'a' as Character        || 97 as Byte
        Byte      | null                    || null
        // char
        char      | 1 as byte               || 1 as char
        char      | 1 as Byte               || 1 as char
        char      | 1 as short              || 1 as char
        char      | 1 as Short              || 1 as char
        char      | 1 as int                || 1 as char
        char      | 1 as Integer            || 1 as char
        char      | 1 as long               || 1 as char
        char      | 1 as Long               || 1 as char
        char      | 1 as float              || 1 as char
        char      | 1 as Float              || 1 as char
        char      | 1 as double             || 1 as char
        char      | 1 as Double             || 1 as char
        char      | 'a' as char             || 'a' as char
        char      | 'a' as Character        || 'a' as char
        // Character
        Character | 1 as byte               || 1 as Character
        Character | 1 as Byte               || 1 as Character
        Character | 1 as short              || 1 as Character
        Character | 1 as Short              || 1 as Character
        Character | 1 as int                || 1 as Character
        Character | 1 as Integer            || 1 as Character
        Character | 1 as long               || 1 as Character
        Character | 1 as Long               || 1 as Character
        Character | 1 as float              || 1 as Character
        Character | 1 as Float              || 1 as Character
        Character | 1 as double             || 1 as Character
        Character | 1 as Double             || 1 as Character
        Character | 'a' as char             || 'a' as Character
        Character | 'a' as Character        || 'a' as Character
        Character | null                    || null
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
        Short     | 1 as byte               || 1 as Short
        Short     | 1 as Byte               || 1 as Short
        Short     | 1 as short              || 1 as Short
        Short     | 1 as Short              || 1 as Short
        Short     | 1 as int                || 1 as Short
        Short     | 1 as Integer            || 1 as Short
        Short     | 1 as long               || 1 as Short
        Short     | 1 as Long               || 1 as Short
        Short     | 1 as float              || 1 as Short
        Short     | 1 as Float              || 1 as Short
        Short     | 1 as double             || 1 as Short
        Short     | 1 as Double             || 1 as Short
        Short     | 'a' as char             || 97 as Short
        Short     | 'a' as Character        || 97 as Short
        Short     | null                    || null
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
        Integer   | 1 as byte               || 1 as Integer
        Integer   | 1 as Byte               || 1 as Integer
        Integer   | 1 as short              || 1 as Integer
        Integer   | 1 as Short              || 1 as Integer
        Integer   | 1 as int                || 1 as Integer
        Integer   | 1 as Integer            || 1 as Integer
        Integer   | 1 as long               || 1 as Integer
        Integer   | 1 as Long               || 1 as Integer
        Integer   | 1 as float              || 1 as Integer
        Integer   | 1 as Float              || 1 as Integer
        Integer   | 1 as double             || 1 as Integer
        Integer   | 1 as Double             || 1 as Integer
        Integer   | 'a' as char             || 97 as Integer
        Integer   | 'a' as Character        || 97 as Integer
        Integer   | null                    || null
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
        Long      | 1 as byte               || 1 as Long
        Long      | 1 as Byte               || 1 as Long
        Long      | 1 as short              || 1 as Long
        Long      | 1 as Short              || 1 as Long
        Long      | 1 as int                || 1 as Long
        Long      | 1 as Integer            || 1 as Long
        Long      | 1 as long               || 1 as Long
        Long      | 1 as Long               || 1 as Long
        Long      | 1 as float              || 1 as Long
        Long      | 1 as Float              || 1 as Long
        Long      | 1 as double             || 1 as Long
        Long      | 1 as Double             || 1 as Long
        Long      | 'a' as char             || 97 as Long
        Long      | 'a' as Character        || 97 as Long
        Long      | null                    || null
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
        Float     | 1 as byte               || 1 as Float
        Float     | 1 as Byte               || 1 as Float
        Float     | 1 as short              || 1 as Float
        Float     | 1 as Short              || 1 as Float
        Float     | 1 as int                || 1 as Float
        Float     | 1 as Integer            || 1 as Float
        Float     | 1 as long               || 1 as Float
        Float     | 1 as Long               || 1 as Float
        Float     | 1 as float              || 1 as Float
        Float     | 1 as Float              || 1 as Float
        Float     | 1 as double             || 1 as Float
        Float     | 1 as Double             || 1 as Float
        Float     | 'a' as char             || 97 as Float
        Float     | 'a' as Character        || 97 as Float
        Float     | null                    || null
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
        Double    | 1 as byte               || 1 as Double
        Double    | 1 as Byte               || 1 as Double
        Double    | 1 as short              || 1 as Double
        Double    | 1 as Short              || 1 as Double
        Double    | 1 as int                || 1 as Double
        Double    | 1 as Integer            || 1 as Double
        Double    | 1 as long               || 1 as Double
        Double    | 1 as Long               || 1 as Double
        Double    | 1 as float              || 1 as Double
        Double    | 1 as Float              || 1 as Double
        Double    | 1 as double             || 1 as Double
        Double    | 1 as Double             || 1 as Double
        Double    | 'a' as char             || 97 as Double
        Double    | 'a' as Character        || 97 as Double
        Double    | null                    || null
        // boolean
        boolean   | true as boolean         || true as boolean
        boolean   | Boolean.TRUE as Boolean || true as Boolean
        // Boolean
        Boolean   | true as boolean         || true as boolean
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
