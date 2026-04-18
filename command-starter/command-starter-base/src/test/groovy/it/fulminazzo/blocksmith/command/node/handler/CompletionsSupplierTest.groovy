//file:noinspection unused
package it.fulminazzo.blocksmith.command.node.handler

import groovy.transform.EqualsAndHashCode
import it.fulminazzo.blocksmith.reflect.Reflect
import it.fulminazzo.blocksmith.reflect.ReflectException
import spock.lang.Specification

import java.lang.reflect.Method

@EqualsAndHashCode
class CompletionsSupplierTest extends Specification {
    private static Method valid = CompletionsSupplierTest.getMethod('valid')
    private static Method validStatic = CompletionsSupplierTest.getMethod('validStatic')
    private static Method invalidReturnType = CompletionsSupplierTest.getMethod('invalidReturnType')
    private static Method invalidReturnTypeStatic = CompletionsSupplierTest.getMethod('invalidReturnTypeStatic')

    def 'test that get works'() {
        given:
        def supplier = new CompletionsSupplier(this, valid)

        when:
        def actual = supplier.get()

        then:
        actual == ['first', 'null', 'third', '"fourth completion"']
    }

    def 'test that get throws Exception'() {
        given:
        def supplier = new CompletionsSupplier(
                this,
                CompletionsSupplierTest.getMethod('exception')
        )

        when:
        supplier.get()

        then:
        def e = thrown(ReflectException)
        def cause = e.cause
        (cause.class == Exception)
        cause.message == 'Test exception'
    }

    def 'test that get throws Runtime exception'() {
        given:
        def supplier = new CompletionsSupplier(
                this,
                CompletionsSupplierTest.getMethod('runtimeException')
        )

        when:
        supplier.get()

        then:
        def e = thrown(ReflectException)
        def cause = e.cause
        (cause.class == RuntimeException)
        cause.message == 'Test runtime exception'
    }

    def 'test that of function with #methodDeclaration returns #expectedRequester, #expectedMethod'() {
        when:
        def supplier = CompletionsSupplier.of(this, methodDeclaration)

        then:
        supplier.executor == Reflect.on(expectedRequester())
        supplier.method == expectedMethod

        where:
        methodDeclaration                                              || expectedRequester           | expectedMethod
        "${valid.name}"                                                || { this }                    | valid
        "${validStatic.name}"                                          || { CompletionsSupplierTest } | validStatic
        "${CompletionsSupplierTest.canonicalName}.${validStatic.name}" || { CompletionsSupplierTest } | validStatic
    }

    def 'test that of function with #methodDeclaration throws with #message'() {
        when:
        CompletionsSupplier.of(requester(), methodDeclaration)

        then:
        def e = thrown(ReflectException)
        e.message == message

        where:
        methodDeclaration                                                          | requester                   || message
        'not.existing.Class.method'                                                | { this }                    || 'Could not find class \'not.existing.Class\''
        "${CompletionsSupplierTest.canonicalName}.${valid.name}"                   | { CompletionsSupplierTest } ||
                "Invalid method ${List.canonicalName}<${String.canonicalName}> ${valid.name}() in type ${CompletionsSupplierTest.canonicalName}: " +
                "completions functions with class executor must be static"
        "${invalidReturnType.name}"                                                | { this }                    ||
                "Invalid method ${String.canonicalName} ${invalidReturnType.name}() in type ${CompletionsSupplierTest.canonicalName}: " +
                "must return an instance of ${Collection.canonicalName}"
        "${invalidReturnTypeStatic.name}"                                          | { this }                    ||
                "Invalid method ${String.canonicalName} ${invalidReturnTypeStatic.name}() in type ${CompletionsSupplierTest.canonicalName}: " +
                "must return an instance of ${Collection.canonicalName}"
        "${CompletionsSupplierTest.canonicalName}.${invalidReturnTypeStatic.name}" | { this }                    ||
                "Invalid method ${String.canonicalName} ${invalidReturnTypeStatic.name}() in type ${CompletionsSupplierTest.canonicalName}: " +
                "must return an instance of ${Collection.canonicalName}"
        'notFound'                                                                 | { this }                    ||
                "Could not find method ? notFound() in type '${CompletionsSupplierTest.canonicalName}'"
        'notFoundStatic'                                                           | { this }                    ||
                "Could not find method ? notFoundStatic() in type '${CompletionsSupplierTest.canonicalName}'"
        "${CompletionsSupplierTest.canonicalName}.notFoundStatic"                  | { this }                    ||
                "Could not find method ? notFoundStatic() in type '${CompletionsSupplierTest.canonicalName}'"
    }

    List<String> valid() {
        return ['first', null, 'third', 'fourth completion']
    }

    List<?> exception() {
        throw new Exception('Test exception')
    }

    List<?> runtimeException() {
        throw new RuntimeException('Test runtime exception')
    }

    private List<String> privateMethod() {
        return []
    }

    String invalidReturnType() {
        return ''
    }

    static List<String> validStatic() {
        return []
    }

    static String invalidReturnTypeStatic() {
        return ''
    }

}
