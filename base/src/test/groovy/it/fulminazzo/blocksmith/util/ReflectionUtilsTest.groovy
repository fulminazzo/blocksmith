package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class ReflectionUtilsTest extends Specification {

    def 'test that initialize correctly initializes #type'() {
        when:
        ReflectionUtils.initialize(
                type,
                parametersTypes,
                parameters.toArray()
        )

        then:
        noExceptionThrown()

        where:
        type       | parametersTypes  | parameters
        JavaBean   | []               | []
        MockObject | [String, String] | ['Alex', 'Fulminazzo']
    }

    def 'test that initialize throws #expected for #type'() {
        when:
        ReflectionUtils.initialize(
                type,
                parametersTypes,
                parameters.toArray()
        )

        then:
        def actual = thrown(expected.class)
        actual.message == expected.message

        and:
        def eCause = expected.cause
        def cause = actual.cause
        if (eCause == null) {
            assert cause == null
        } else {
            cause.class == eCause.class
            cause.message == eCause.message
        }

        where:
        type                        | parametersTypes | parameters || expected
        JavaBean                    | [String]        | ['Alex']   || new ReflectionUtils.ReflectionException(
                "Could not find constructor ${ReflectionUtilsTest.canonicalName}.JavaBean(${String.canonicalName})"
        )
        ExceptionConstructor        | []              | []         || new RuntimeException(
                new Exception('Test exception')
        )
        RuntimeExceptionConstructor | []              | []         || new RuntimeException('Test runtime exception')
    }

    def 'test that getInstanceFields returns only non-static fields'() {
        when:
        def actual = ReflectionUtils.getInstanceFields(JavaBean)

        then:
        actual == [
                JavaBean.getDeclaredField('name'),
                Parent.getDeclaredField('version')
        ]
    }

    def 'test that invokeMethod works'() {
        when:
        def actual = ReflectionUtils.invokeMethod(Integer, 'parseInt', [String], '1')

        then:
        actual == 1
    }

    def 'test that invokeMethod is able to invoke methods from superclass'() {
        when:
        def actual = ReflectionUtils.invokeMethod(new JavaBean(), 'message', [])

        then:
        actual == 'Hello, world!'
    }

    def 'test that invokeMethod with #methodName throws #expected'() {
        when:
        ReflectionUtils.invokeMethod(JavaBean, methodName, [])

        then:
        def actual = thrown(expected.class)
        actual.message == expected.message

        and:
        def eCause = expected.cause
        def aCause = actual.cause
        if (eCause == null) assert aCause == null
        else {
            assert aCause != null
            assert eCause.class == aCause.class
            assert eCause.message == aCause.message
        }

        where:
        methodName         || expected
        'notFound'         || new ReflectionUtils.ReflectionException(
                "Could not invoke method 'notFound' from '${JavaBean.canonicalName}': " +
                        "no such method was found")
        'exception'        || new RuntimeException(new Exception('Test exception'))
        'runtimeException' || new RuntimeException('Test runtime exception')
    }

    static class Parent {
        double version
        static String parentName

        String message() {
            return 'Hello, world!'
        }

    }

    static class JavaBean extends Parent {
        String name
        static String className

        static void exception() {
            throw new Exception('Test exception')
        }

        static void runtimeException() {
            throw new RuntimeException('Test runtime exception')
        }

    }

    static class MockObject {
        final String name
        final String lastname

        MockObject(String name, String lastname) {
            this.name = name
            this.lastname = lastname
        }

    }

    static class ExceptionConstructor {

        ExceptionConstructor() {
            throw new Exception('Test exception')
        }

    }

    static class RuntimeExceptionConstructor {

        RuntimeExceptionConstructor() {
            throw new RuntimeException('Test runtime exception')
        }

    }

}
