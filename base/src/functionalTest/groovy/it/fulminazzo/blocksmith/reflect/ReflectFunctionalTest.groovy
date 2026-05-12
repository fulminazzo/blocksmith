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
    private static final Constructor<?> constructor = Person.getDeclaredConstructor(String, Integer)

    /*
     * TEST SUBJECTS
     */
    private static final Field interfaceStaticField = Entity.getDeclaredField('ENTITIES_DEFAULT_NAME')
    private static final Field superStaticField = NamedEntity.getDeclaredField('defaultName')
    private static final Field staticField = Person.getDeclaredField('defaultAge')
    private static final Field superField = NamedEntity.getDeclaredField('name')
    private static final Field field = Person.getDeclaredField('age')

    private static final Method interfaceDefaultMethodNoArgs = Entity.getDeclaredMethod('getUniqueId')
    private static final Method interfaceMethodNoArgs = Entity.getDeclaredMethod('getName')
    private static final Method superStaticMethodNoArgs = NamedEntity.getDeclaredMethod('getDefaultName')
    private static final Method staticMethodNoArgs = Person.getDeclaredMethod('getDefaultAge')
    private static final Method superMethodNoArgs = NamedEntity.getDeclaredMethod('getName')
    private static final Method methodNoArgs = Person.getDeclaredMethod('getAge')

    private static final Method superStaticMethod = NamedEntity.getDeclaredMethod('setDefaultName', String)
    private static final Method staticMethod = Person.getDeclaredMethod('setDefaultAge', Integer)
    private static final Method superMethod = NamedEntity.getDeclaredMethod('setName', String)
    private static final Method objectMethod = Person.getDeclaredMethod('setAge', Integer)

    private static final Method superEquals = NamedEntity.getDeclaredMethod('equals', Object)
    private static final Method superHashCode = NamedEntity.getDeclaredMethod('hashCode')
    private static final Method superToString = NamedEntity.getDeclaredMethod('toString')
    private static final Method superLombokCanEqual = NamedEntity.getDeclaredMethod('canEqual', Object)

    private static final Method equals = Person.getDeclaredMethod('equals', Object)
    private static final Method hashCode = Person.getDeclaredMethod('hashCode')
    private static final Method toString = Person.getDeclaredMethod('toString')
    private static final Method lombokCanEqual = Person.getDeclaredMethod('canEqual', Object)

    /*
     * TEST ARGUMENTS
     */
    private static final Predicate<Field> inInterface = (Predicate<Field>) (f) -> f.declaringClass == Entity
    private static final Predicate<Field> inSuper = (Predicate<Field>) (f) -> f.declaringClass == NamedEntity
    private static final Predicate<Field> inInstance = (Predicate<Field>) (f) -> f.declaringClass == Person
    private static final Predicate<Field> truePredicate = (Predicate<Field>) (f) -> true
    private static final Predicate<Field> falsePredicate = (Predicate<Field>) (f) -> false
    private static final Predicate<Field> inOther = (Predicate<Field>) (f) -> f.declaringClass == String

    /*
     * TEST RESULTS
     */
    private static final Object expectedInterfaceStaticFieldValue = 'Steve'
    private static final Object expectedSuperStaticFieldValue = 'John'
    private static final Object expectedStaticFieldValue = 18

    private static final List<Method> expectedInterfaceMethods = [interfaceMethodNoArgs, interfaceDefaultMethodNoArgs]
    private static final List<Method> expectedSuperMethods = [
            superLombokCanEqual, superEquals, superMethodNoArgs, superHashCode, superMethod, superToString
    ]
    private static final List<Method> expectedMethods = [lombokCanEqual, equals, methodNoArgs, hashCode, objectMethod, toString]
    private static final List<Method> objectMethods = Object.declaredMethods
            .findAll { !it.synthetic && !it.bridge && !Modifier.isStatic(it.modifiers) }
            .sort { a, b -> a.name <=> b.name ?: a.parameterCount <=> b.parameterCount }

    private static final List<Method> expectedSuperStaticMethods = [superStaticMethodNoArgs, superStaticMethod]
    private static final List<Method> expectedStaticMethods = [staticMethodNoArgs, staticMethod]
    private static final List<Method> objectStaticMethods = Object.declaredMethods
            .findAll { !it.synthetic && !it.bridge && Modifier.isStatic(it.modifiers) }
            .sort { a, b -> a.name <=> b.name ?: a.parameterCount <=> b.parameterCount }

    private static final String superValue = 'Alex'
    private static final UUID interfaceValue = UUID.nameUUIDFromBytes(superValue.bytes)
    private static final int objectValue = 23

    static {
        interfaceStaticField.accessible = true
        superStaticField.accessible = true
        staticField.accessible = true
    }

    private Reflect reflect

    void setup() {
        reflect = new Reflect(Person, new Person(superValue, objectValue))
        resetStaticFields()
    }

    void cleanup() {
        resetStaticFields()
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
        method                    | arguments                                     || expected
        // init
        'init'                    | ['Camilla', 21]                               || new Reflect(Person, new Person('Camilla', 21))
        'init'                    | [[null, 21].toArray()]                        || new Reflect(Person, new Person(null, 21))
        'init'                    | ['Camilla', null]                             || new Reflect(Person, new Person('Camilla', null))
        'init'                    | [[null, null].toArray()]                      || new Reflect(Person, new Person(null, null))
        // getConstructor
        'getConstructor'          | [String, Integer]                             || constructor
        'getConstructor'          | [inInstance]                                  || constructor
        'getConstructor'          | [truePredicate]                               || constructor
        // getConstructors
        'getConstructors'         | [falsePredicate]                              || []
        'getConstructors'         | [inSuper]                                     || []
        'getConstructors'         | [inInstance]                                  || [constructor]
        'getConstructors'         | [truePredicate]                               || [constructor]
        'getConstructors'         | []                                            || [constructor]
        // getFieldValues
        'getInstanceFieldValues'  | []                                            || [objectValue].collect { new Reflect(it.getClass(), it) }
        'getNonStaticFieldValues' | []                                            || [objectValue, superValue]
                .collect { new Reflect(it.getClass(), it) }
        'getStaticFieldValues'    | []                                            || [
                expectedStaticFieldValue,
                expectedSuperStaticFieldValue,
                expectedInterfaceStaticFieldValue].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [falsePredicate]                              || [].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [inInterface]                                 || [expectedInterfaceStaticFieldValue].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [inSuper]                                     || [expectedSuperStaticFieldValue, superValue].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [inInstance]                                  || [expectedStaticFieldValue, objectValue].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | [truePredicate]                               || [
                expectedStaticFieldValue, objectValue,
                expectedSuperStaticFieldValue, superValue,
                expectedInterfaceStaticFieldValue].collect { new Reflect(it.getClass(), it) }
        'getFieldValues'          | []                                            || [
                expectedStaticFieldValue, objectValue,
                expectedSuperStaticFieldValue, superValue,
                expectedInterfaceStaticFieldValue].collect { new Reflect(it.getClass(), it) }
        // set
        'setInstance'             | [field.name, objectValue]                     || new Reflect(Person, new Person(superValue, objectValue))
        'setNonStatic'            | [superField.name, superValue]                 || new Reflect(Person, new Person(superValue, objectValue))
        'setNonStatic'            | [field.name, objectValue]                     || new Reflect(Person, new Person(superValue, objectValue))
        'setStatic'               | [superStaticField.name,
                                     expectedSuperStaticFieldValue]               || new Reflect(Person, new Person(superValue, objectValue))
        'setStatic'               | [staticField.name, expectedStaticFieldValue]  || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [superStaticField.name,
                                     expectedSuperStaticFieldValue]               || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [staticField.name,
                                     expectedStaticFieldValue]                    || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [superField.name, superValue]                 || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [field.name, objectValue]                     || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [inSuper,
                                     expectedSuperStaticFieldValue]               || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [inInstance,
                                     expectedStaticFieldValue]                    || new Reflect(Person, new Person(superValue, objectValue))
        'set'                     | [truePredicate,
                                     expectedStaticFieldValue]                    || new Reflect(Person, new Person(superValue, objectValue))
        // get orElse
        'getInstance'             | [superStaticField.name, 'unknown']            || new Reflect(String, 'unknown')
        'getInstance'             | [superStaticField.name, null]                 || new Reflect(null, null)
        'getInstance'             | [staticField.name, 15]                        || new Reflect(Integer, 15)
        'getInstance'             | [staticField.name, null]                      || new Reflect(null, null)
        'getInstance'             | [superField.name, 'unknown']                  || new Reflect(String, 'unknown')
        'getInstance'             | [superField.name, null]                       || new Reflect(null, null)
        'getInstance'             | [field.name, 15]                              || new Reflect(field.type, objectValue)
        'getInstance'             | [field.name, null]                            || new Reflect(field.type, objectValue)
        'getNonStatic'            | [superStaticField.name, 'unknown']            || new Reflect(String, 'unknown')
        'getNonStatic'            | [superStaticField.name, null]                 || new Reflect(null, null)
        'getNonStatic'            | [staticField.name, 15]                        || new Reflect(Integer, 15)
        'getNonStatic'            | [staticField.name, null]                      || new Reflect(null, null)
        'getNonStatic'            | [superField.name, 'unknown']                  || new Reflect(superField.type, superValue)
        'getNonStatic'            | [superField.name, null]                       || new Reflect(superField.type, superValue)
        'getNonStatic'            | [field.name, 15]                              || new Reflect(field.type, objectValue)
        'getNonStatic'            | [field.name, null]                            || new Reflect(field.type, objectValue)
        'getStatic'               | [superStaticField.name, 'unknown']            || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'getStatic'               | [superStaticField.name, null]                 || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'getStatic'               | [interfaceStaticField.name, 'unknown']        || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'getStatic'               | [interfaceStaticField.name, null]             || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'getStatic'               | [staticField.name, 15]                        || new Reflect(staticField.type, expectedStaticFieldValue)
        'getStatic'               | [staticField.name, null]                      || new Reflect(staticField.type, expectedStaticFieldValue)
        'getStatic'               | [superField.name, 'unknown']                  || new Reflect(String, 'unknown')
        'getStatic'               | [superField.name, null]                       || new Reflect(null, null)
        'getStatic'               | [field.name, 15]                              || new Reflect(Integer, 15)
        'getStatic'               | [field.name, null]                            || new Reflect(null, null)
        'get'                     | [interfaceStaticField.name, 'unknown']        || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'get'                     | [interfaceStaticField.name, null]             || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'get'                     | [superStaticField.name, 'unknown']            || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [superStaticField.name, null]                 || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [staticField.name, 15]                        || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [staticField.name, null]                      || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [superField.name, 'unknown']                  || new Reflect(superField.type, superValue)
        'get'                     | [superField.name, null]                       || new Reflect(superField.type, superValue)
        'get'                     | [field.name, 15]                              || new Reflect(field.type, objectValue)
        'get'                     | [field.name, null]                            || new Reflect(field.type, objectValue)
        'get'                     | ['unknown', 'unknown']                        || new Reflect(String, 'unknown')
        'get'                     | ['unknown', null]                             || new Reflect(null, null)
        'get'                     | [falsePredicate, 'unknown']                   || new Reflect(String, 'unknown')
        'get'                     | [falsePredicate, null]                        || new Reflect(null, null)
        'get'                     | [inInterface, 'unknown']                      || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'get'                     | [inInterface, null]                           || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'get'                     | [inSuper, 'unknown']                          || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [inSuper, null]                               || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [inInstance, 'unknown']                       || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [inInstance, null]                            || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [truePredicate, 'unknown']                    || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [truePredicate, null]                         || new Reflect(staticField.type, expectedStaticFieldValue)
        // get
        'getInstance'             | [field.name]                                  || new Reflect(field.type, objectValue)
        'getNonStatic'            | [superField.name]                             || new Reflect(superField.type, superValue)
        'getNonStatic'            | [field.name]                                  || new Reflect(field.type, objectValue)
        'getStatic'               | [interfaceStaticField.name]                   || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'getStatic'               | [superStaticField.name]                       || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'getStatic'               | [staticField.name]                            || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [interfaceStaticField.name]                   || new Reflect(interfaceStaticField.type, expectedInterfaceStaticFieldValue)
        'get'                     | [superStaticField.name]                       || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [staticField.name]                            || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [superField.name]                             || new Reflect(superField.type, superValue)
        'get'                     | [field.name]                                  || new Reflect(field.type, objectValue)
        'get'                     | [inSuper]                                     || new Reflect(superStaticField.type, expectedSuperStaticFieldValue)
        'get'                     | [inInstance]                                  || new Reflect(staticField.type, expectedStaticFieldValue)
        'get'                     | [truePredicate]                               || new Reflect(staticField.type, expectedStaticFieldValue)
        // getField
        'getInstanceField'        | [field.name]                                  || field
        'getNonStaticField'       | [superField.name]                             || superField
        'getNonStaticField'       | [field.name]                                  || field
        'getStaticField'          | [interfaceStaticField.name]                   || interfaceStaticField
        'getStaticField'          | [superStaticField.name]                       || superStaticField
        'getStaticField'          | [staticField.name]                            || staticField
        'getField'                | [interfaceStaticField.name]                   || interfaceStaticField
        'getField'                | [superStaticField.name]                       || superStaticField
        'getField'                | [staticField.name]                            || staticField
        'getField'                | [superField.name]                             || superField
        'getField'                | [field.name]                                  || field
        'getField'                | [inSuper]                                     || superStaticField
        'getField'                | [inInstance]                                  || staticField
        'getField'                | [truePredicate]                               || staticField
        // getFields
        'getInstanceFields'       | []                                            || [field]
        'getNonStaticFields'      | []                                            || [field, superField]
        'getStaticFields'         | []                                            || [staticField, superStaticField, interfaceStaticField]
        'getFields'               | [falsePredicate]                              || []
        'getFields'               | [inInterface]                                 || [interfaceStaticField]
        'getFields'               | [inSuper]                                     || [superStaticField, superField]
        'getFields'               | [inInstance]                                  || [staticField, field]
        'getFields'               | [truePredicate]                               || [staticField, field, superStaticField, superField, interfaceStaticField]
        'getFields'               | []                                            || [staticField, field, superStaticField, superField, interfaceStaticField]
        // invoke
        'invoke'                  | [interfaceDefaultMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(interfaceValue.getClass(), interfaceValue)
        'invoke'                  | [interfaceDefaultMethodNoArgs.name]           || new Reflect(interfaceValue.getClass(), interfaceValue)
        'invoke'                  | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(interfaceValue.getClass(), interfaceValue)
        'invoke'                  | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name]           || new Reflect(interfaceValue.getClass(), interfaceValue)
        'invoke'                  | [superStaticMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(expectedSuperStaticFieldValue.getClass(), expectedSuperStaticFieldValue)
        'invoke'                  | [superStaticMethodNoArgs.name]                || new Reflect(expectedSuperStaticFieldValue.getClass(), expectedSuperStaticFieldValue)
        'invoke'                  | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(expectedSuperStaticFieldValue.getClass(), expectedSuperStaticFieldValue)
        'invoke'                  | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name]                || new Reflect(expectedSuperStaticFieldValue.getClass(), expectedSuperStaticFieldValue)
        'invoke'                  | [superStaticMethod.name,
                                     [expectedSuperStaticFieldValue].toArray()]   || new Reflect(void, null)
        'invoke'                  | [superStaticMethod.returnType,
                                     superStaticMethod.name,
                                     [expectedSuperStaticFieldValue].toArray()]   || new Reflect(void, null)
        'invoke'                  | [superMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(superValue.getClass(), superValue)
        'invoke'                  | [superMethodNoArgs.name]                      || new Reflect(superValue.getClass(), superValue)
        'invoke'                  | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(superValue.getClass(), superValue)
        'invoke'                  | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name]                      || new Reflect(superValue.getClass(), superValue)
        'invoke'                  | [[superValue].toArray()]                      || new Reflect(void, null)
        'invoke'                  | [superMethod.name,
                                     [superValue].toArray()]                      || new Reflect(void, null)
        'invoke'                  | [superMethod.returnType,
                                     superMethod.name,
                                     [superValue].toArray()]                      || new Reflect(void, null)
        'invoke'                  | [staticMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(expectedStaticFieldValue.getClass(), expectedStaticFieldValue)
        'invoke'                  | [staticMethodNoArgs.name]                     || new Reflect(expectedStaticFieldValue.getClass(), expectedStaticFieldValue)
        'invoke'                  | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name,
                                     [].toArray()]                                || new Reflect(expectedStaticFieldValue.getClass(), expectedStaticFieldValue)
        'invoke'                  | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name]                     || new Reflect(expectedStaticFieldValue.getClass(), expectedStaticFieldValue)
        'invoke'                  | [staticMethod.name,
                                     [expectedStaticFieldValue].toArray()]        || new Reflect(void, null)
        'invoke'                  | [staticMethod.returnType,
                                     staticMethod.name,
                                     [expectedStaticFieldValue].toArray()]        || new Reflect(void, null)
        'invoke'                  | [methodNoArgs.name,
                                     [].toArray()]                                || new Reflect(objectValue.getClass(), objectValue)
        'invoke'                  | [methodNoArgs.name]                           || new Reflect(objectValue.getClass(), objectValue)
        'invoke'                  | [methodNoArgs.returnType,
                                     methodNoArgs.name,
                                     [].toArray()]                                || new Reflect(objectValue.getClass(), objectValue)
        'invoke'                  | [methodNoArgs.returnType,
                                     methodNoArgs.name]                           || new Reflect(objectValue.getClass(), objectValue)
        'invoke'                  | [[objectValue].toArray()]                     || new Reflect(void, null)
        'invoke'                  | [objectMethod.name,
                                     [objectValue].toArray()]                     || new Reflect(void, null)
        'invoke'                  | [objectMethod.returnType,
                                     objectMethod.name,
                                     [objectValue].toArray()]                     || new Reflect(void, null)
        'invoke'                  | []                                            || new Reflect(objectValue.getClass(), objectValue)
        // getMethod
        'getInstanceMethod'       | [methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getInstanceMethod'       | [methodNoArgs.name]                           || methodNoArgs
        'getInstanceMethod'       | [methodNoArgs.returnType,
                                     methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getInstanceMethod'       | [methodNoArgs.returnType,
                                     methodNoArgs.name]                           || methodNoArgs
        'getInstanceMethod'       | [objectMethod.parameterTypes]                 || objectMethod
        'getInstanceMethod'       | [objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getInstanceMethod'       | [objectMethod.returnType,
                                     objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getInstanceMethod'       | []                                            || methodNoArgs
        'getNonStaticMethod'      | [interfaceDefaultMethodNoArgs.name,
                                     interfaceDefaultMethodNoArgs.parameterTypes] || interfaceDefaultMethodNoArgs
        'getNonStaticMethod'      | [interfaceDefaultMethodNoArgs.name]           || interfaceDefaultMethodNoArgs
        'getNonStaticMethod'      | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name,
                                     interfaceDefaultMethodNoArgs.parameterTypes] || interfaceDefaultMethodNoArgs
        'getNonStaticMethod'      | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name]           || interfaceDefaultMethodNoArgs
        'getNonStaticMethod'      | [superMethodNoArgs.name,
                                     superMethodNoArgs.parameterTypes]            || superMethodNoArgs
        'getNonStaticMethod'      | [superMethodNoArgs.name]                      || superMethodNoArgs
        'getNonStaticMethod'      | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name,
                                     superMethodNoArgs.parameterTypes]            || superMethodNoArgs
        'getNonStaticMethod'      | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name]                      || superMethodNoArgs
        'getNonStaticMethod'      | [superMethod.parameterTypes]                  || superMethod
        'getNonStaticMethod'      | [superMethod.name,
                                     superMethod.parameterTypes]                  || superMethod
        'getNonStaticMethod'      | [superMethod.returnType,
                                     superMethod.name,
                                     superMethod.parameterTypes]                  || superMethod
        'getNonStaticMethod'      | [methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getNonStaticMethod'      | [methodNoArgs.name]                           || methodNoArgs
        'getNonStaticMethod'      | [methodNoArgs.returnType,
                                     methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getNonStaticMethod'      | [methodNoArgs.returnType,
                                     methodNoArgs.name]                           || methodNoArgs
        'getNonStaticMethod'      | [objectMethod.parameterTypes]                 || objectMethod
        'getNonStaticMethod'      | [objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getNonStaticMethod'      | [objectMethod.returnType,
                                     objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getNonStaticMethod'      | []                                            || methodNoArgs
        'getStaticMethod'         | [superStaticMethodNoArgs.name,
                                     superStaticMethodNoArgs.parameterTypes]      || superStaticMethodNoArgs
        'getStaticMethod'         | [superStaticMethodNoArgs.name]                || superStaticMethodNoArgs
        'getStaticMethod'         | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name,
                                     superStaticMethodNoArgs.parameterTypes]      || superStaticMethodNoArgs
        'getStaticMethod'         | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name]                || superStaticMethodNoArgs
        'getStaticMethod'         | [superStaticMethod.parameterTypes]            || superStaticMethod
        'getStaticMethod'         | [superStaticMethod.name,
                                     superStaticMethod.parameterTypes]            || superStaticMethod
        'getStaticMethod'         | [superStaticMethod.returnType,
                                     superStaticMethod.name,
                                     superStaticMethod.parameterTypes]            || superStaticMethod
        'getStaticMethod'         | [staticMethodNoArgs.name,
                                     staticMethodNoArgs.parameterTypes]           || staticMethodNoArgs
        'getStaticMethod'         | [staticMethodNoArgs.name]                     || staticMethodNoArgs
        'getStaticMethod'         | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name,
                                     staticMethodNoArgs.parameterTypes]           || staticMethodNoArgs
        'getStaticMethod'         | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name]                     || staticMethodNoArgs
        'getStaticMethod'         | [staticMethod.parameterTypes]                 || staticMethod
        'getStaticMethod'         | [staticMethod.name,
                                     staticMethod.parameterTypes]                 || staticMethod
        'getStaticMethod'         | [staticMethod.returnType,
                                     staticMethod.name,
                                     staticMethod.parameterTypes]                 || staticMethod
        'getStaticMethod'         | []                                            || staticMethodNoArgs
        'getMethod'               | [interfaceDefaultMethodNoArgs.name,
                                     interfaceDefaultMethodNoArgs.parameterTypes] || interfaceDefaultMethodNoArgs
        'getMethod'               | [interfaceDefaultMethodNoArgs.name]           || interfaceDefaultMethodNoArgs
        'getMethod'               | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name,
                                     interfaceDefaultMethodNoArgs.parameterTypes] || interfaceDefaultMethodNoArgs
        'getMethod'               | [interfaceDefaultMethodNoArgs.returnType,
                                     interfaceDefaultMethodNoArgs.name]           || interfaceDefaultMethodNoArgs
        'getMethod'               | [superStaticMethodNoArgs.name,
                                     superStaticMethodNoArgs.parameterTypes]      || superStaticMethodNoArgs
        'getMethod'               | [superStaticMethodNoArgs.name]                || superStaticMethodNoArgs
        'getMethod'               | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name,
                                     superStaticMethodNoArgs.parameterTypes]      || superStaticMethodNoArgs
        'getMethod'               | [superStaticMethodNoArgs.returnType,
                                     superStaticMethodNoArgs.name]                || superStaticMethodNoArgs
        'getMethod'               | [superStaticMethod.name,
                                     superStaticMethod.parameterTypes]            || superStaticMethod
        'getMethod'               | [superStaticMethod.returnType,
                                     superStaticMethod.name,
                                     superStaticMethod.parameterTypes]            || superStaticMethod
        'getMethod'               | [superMethodNoArgs.name,
                                     superMethodNoArgs.parameterTypes]            || superMethodNoArgs
        'getMethod'               | [superMethodNoArgs.name]                      || superMethodNoArgs
        'getMethod'               | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name,
                                     superMethodNoArgs.parameterTypes]            || superMethodNoArgs
        'getMethod'               | [superMethodNoArgs.returnType,
                                     superMethodNoArgs.name]                      || superMethodNoArgs
        'getMethod'               | [superMethod.parameterTypes]                  || superMethod
        'getMethod'               | [superMethod.name,
                                     superMethod.parameterTypes]                  || superMethod
        'getMethod'               | [superMethod.returnType,
                                     superMethod.name,
                                     superMethod.parameterTypes]                  || superMethod
        'getMethod'               | [staticMethodNoArgs.name,
                                     staticMethodNoArgs.parameterTypes]           || staticMethodNoArgs
        'getMethod'               | [staticMethodNoArgs.name]                     || staticMethodNoArgs
        'getMethod'               | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name,
                                     staticMethodNoArgs.parameterTypes]           || staticMethodNoArgs
        'getMethod'               | [staticMethodNoArgs.returnType,
                                     staticMethodNoArgs.name]                     || staticMethodNoArgs
        'getMethod'               | [staticMethod.name,
                                     staticMethod.parameterTypes]                 || staticMethod
        'getMethod'               | [staticMethod.returnType,
                                     staticMethod.name,
                                     staticMethod.parameterTypes]                 || staticMethod
        'getMethod'               | [methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getMethod'               | [methodNoArgs.name]                           || methodNoArgs
        'getMethod'               | [methodNoArgs.returnType,
                                     methodNoArgs.name,
                                     methodNoArgs.parameterTypes]                 || methodNoArgs
        'getMethod'               | [methodNoArgs.returnType,
                                     methodNoArgs.name]                           || methodNoArgs
        'getMethod'               | [objectMethod.parameterTypes]                 || objectMethod
        'getMethod'               | [objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getMethod'               | [objectMethod.returnType,
                                     objectMethod.name,
                                     objectMethod.parameterTypes]                 || objectMethod
        'getMethod'               | [inSuper]                                     || superLombokCanEqual
        'getMethod'               | [inInstance]                                  || lombokCanEqual
        'getMethod'               | [truePredicate]                               || lombokCanEqual
        'getMethod'               | []                                            || methodNoArgs
        // getMethods
        'getInstanceMethods'      | []                                            || [*expectedMethods]
        'getNonStaticMethods'     | []                                            ||
                [*expectedMethods, *expectedSuperMethods, *expectedInterfaceMethods, *objectMethods]
        'getStaticMethods'        | []                                            ||
                [*expectedStaticMethods, *expectedSuperStaticMethods, *objectStaticMethods]
        'getMethods'              | [falsePredicate]                              || []
        'getMethods'              | [inInterface]                                 || [*expectedInterfaceMethods]
        'getMethods'              | [inSuper]                                     ||
                [*expectedSuperMethods, *expectedSuperStaticMethods]
        'getMethods'              | [inInstance]                                  || [*expectedMethods, *expectedStaticMethods]
        'getMethods'              | [truePredicate]                               ||
                [*expectedMethods, *expectedStaticMethods,
                 *expectedSuperMethods, *expectedSuperStaticMethods,
                 *expectedInterfaceMethods,
                 *objectMethods, *objectStaticMethods]
        'getMethods'              | []                                            ||
                [*expectedMethods, *expectedStaticMethods,
                 *expectedSuperMethods, *expectedSuperStaticMethods,
                 *expectedInterfaceMethods,
                 *objectMethods, *objectStaticMethods]
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
        'getConstructor'     | [falsePredicate]                                                          || ReflectException.cannotFindConstructor(Person)
        // get
        'getInstance'        | [superStaticField.name]                                                   || ReflectException.cannotFindField(Person, superStaticField.name)
        'getInstance'        | [staticField.name]                                                        || ReflectException.cannotFindField(Person, staticField.name)
        'getInstance'        | [superField.name]                                                         || ReflectException.cannotFindField(Person, superField.name)
        'getInstance'        | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStatic'       | [superStaticField.name]                                                   || ReflectException.cannotFindField(Person, superStaticField.name)
        'getNonStatic'       | [staticField.name]                                                        || ReflectException.cannotFindField(Person, staticField.name)
        'getNonStatic'       | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getStatic'          | [superField.name]                                                         || ReflectException.cannotFindField(Person, superField.name)
        'getStatic'          | [field.name]                                                              || ReflectException.cannotFindField(Person, field.name)
        'getStatic'          | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'get'                | [inOther]                                                                 || ReflectException.cannotFindField(Person)
        // getField
        'getInstanceField'   | [superStaticField.name]                                                   || ReflectException.cannotFindField(Person, superStaticField.name)
        'getInstanceField'   | [staticField.name]                                                        || ReflectException.cannotFindField(Person, staticField.name)
        'getInstanceField'   | [superField.name]                                                         || ReflectException.cannotFindField(Person, superField.name)
        'getInstanceField'   | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getNonStaticField'  | [superStaticField.name]                                                   || ReflectException.cannotFindField(Person, superStaticField.name)
        'getNonStaticField'  | [staticField.name]                                                        || ReflectException.cannotFindField(Person, staticField.name)
        'getNonStaticField'  | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getStaticField'     | [superField.name]                                                         || ReflectException.cannotFindField(Person, superField.name)
        'getStaticField'     | [field.name]                                                              || ReflectException.cannotFindField(Person, field.name)
        'getStaticField'     | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | ['notExisting']                                                           || ReflectException.cannotFindField(Person, 'notExisting')
        'getField'           | [inOther]                                                                 || ReflectException.cannotFindField(Person)
        'getField'           | [falsePredicate]                                                          || ReflectException.cannotFindField(Person)
        // invoke
        'invoke'             | [['Hello, world!', null, new Object()].toArray()]                         || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'invoke'             | ['notExisting', ['Hello, world!', null, new Object()].toArray()]          || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'invoke'             | [boolean, 'notExisting', ['Hello, world!', null, new Object()].toArray()] || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        // getMethod
        'getInstanceMethod'  | [superStaticMethodNoArgs.name,
                                superStaticMethodNoArgs.parameterTypes]                                  ||
                ReflectException.cannotFindMethod(Person, null, superStaticMethodNoArgs.name, superStaticMethodNoArgs.parameterTypes)
        'getInstanceMethod'  | [superStaticMethodNoArgs.returnType,
                                superStaticMethodNoArgs.name,
                                superStaticMethodNoArgs.parameterTypes]                                  ||
                ReflectException.cannotFindMethod(Person, superStaticMethodNoArgs.returnType, superStaticMethodNoArgs.name, superStaticMethodNoArgs.parameterTypes)
        'getInstanceMethod'  | [superMethodNoArgs.name,
                                superMethodNoArgs.parameterTypes]                                        ||
                ReflectException.cannotFindMethod(Person, null, superMethodNoArgs.name, superMethodNoArgs.parameterTypes)
        'getInstanceMethod'  | [superMethodNoArgs.returnType,
                                superMethodNoArgs.name,
                                superMethodNoArgs.parameterTypes]                                        ||
                ReflectException.cannotFindMethod(Person, superMethodNoArgs.returnType, superMethodNoArgs.name, superMethodNoArgs.parameterTypes)
        'getInstanceMethod'  | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getInstanceMethod'  | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getInstanceMethod'  | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [superStaticMethodNoArgs.name,
                                superStaticMethodNoArgs.parameterTypes]                                  ||
                ReflectException.cannotFindMethod(Person, null, superStaticMethodNoArgs.name, superStaticMethodNoArgs.parameterTypes)
        'getNonStaticMethod' | [superStaticMethodNoArgs.returnType,
                                superStaticMethodNoArgs.name,
                                superStaticMethodNoArgs.parameterTypes]                                  ||
                ReflectException.cannotFindMethod(Person, superStaticMethodNoArgs.returnType, superStaticMethodNoArgs.name, superStaticMethodNoArgs.parameterTypes)
        'getNonStaticMethod' | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getNonStaticMethod' | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getNonStaticMethod' | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getStaticMethod'    | [superMethodNoArgs.name,
                                superMethodNoArgs.parameterTypes]                                        ||
                ReflectException.cannotFindMethod(Person, null, superMethodNoArgs.name, superMethodNoArgs.parameterTypes)
        'getStaticMethod'    | [superMethodNoArgs.returnType,
                                superMethodNoArgs.name,
                                superMethodNoArgs.parameterTypes]                                        ||
                ReflectException.cannotFindMethod(Person, superMethodNoArgs.returnType, superMethodNoArgs.name, superMethodNoArgs.parameterTypes)
        'getStaticMethod'    | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getStaticMethod'    | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getStaticMethod'    | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [[String, null, Object].toArray(new Class[3])]                            || ReflectException.cannotFindMethod(Person, null, null, String, null, Object)
        'getMethod'          | ['notExisting', [String, null, Object].toArray(new Class[3])]             || ReflectException.cannotFindMethod(Person, null, 'notExisting', String, null, Object)
        'getMethod'          | [boolean, 'notExisting', [String, null, Object].toArray(new Class[3])]    || ReflectException.cannotFindMethod(Person, boolean, 'notExisting', String, null, Object)
        'getMethod'          | [falsePredicate]                                                          || ReflectException.cannotFindMethod(Person)
        // enum
        'name'               | []                                                                        || new ReflectException('%s is not an enum', new Person(superValue, objectValue))
        'ordinal'            | []                                                                        || new ReflectException('%s is not an enum', new Person(superValue, objectValue))
        'getEnum'            | []                                                                        || new ReflectException('%s is not an enum', new Person(superValue, objectValue))
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

    protected static void resetStaticFields() {
        superStaticField.set(null, expectedSuperStaticFieldValue)
        staticField.set(null, expectedStaticFieldValue)
    }

}
