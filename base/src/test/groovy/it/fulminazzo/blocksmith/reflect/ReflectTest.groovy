package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@SuppressWarnings('GroovyAccessibility')
class ReflectTest extends Specification {
    private Reflect reflect

    void setup() {
        reflect = new Reflect(String, '')
    }

    def 'test that init with #exception throws #expected'() {
        given:
        def constructor = Mock(Constructor)
        constructor.newInstance(_) >> {
            throw exception
        }
        constructor.declaringClass >> Object
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
        constructor.declaringClass >> Object
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

}
