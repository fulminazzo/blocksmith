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

    static class JavaBean {}

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
