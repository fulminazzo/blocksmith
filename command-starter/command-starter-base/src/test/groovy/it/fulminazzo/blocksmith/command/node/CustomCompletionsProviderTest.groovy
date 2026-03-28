//file:noinspection unused
package it.fulminazzo.blocksmith.command.node

import groovy.transform.EqualsAndHashCode
import spock.lang.Specification

import java.lang.reflect.Method

@EqualsAndHashCode
class CustomCompletionsProviderTest extends Specification {
    private static Method valid = CustomCompletionsProviderTest.getMethod('valid')
    private static Method validStatic = CustomCompletionsProviderTest.getMethod('validStatic')
    private static Method invalidReturnType = CustomCompletionsProviderTest.getMethod('invalidReturnType')
    private static Method invalidReturnTypeStatic = CustomCompletionsProviderTest.getMethod('invalidReturnTypeStatic')

    def 'test that of function with #methodDeclaration returns #expectedRequester, #expectedMethod'() {
        when:
        def provider = CustomCompletionsProvider.of(this, methodDeclaration)

        then:
        provider.executor == expectedRequester()
        provider.method == expectedMethod

        where:
        methodDeclaration                                                    || expectedRequester                 | expectedMethod
        "${valid.name}"                                                      || { this }                          | valid
        "${validStatic.name}"                                                || { CustomCompletionsProviderTest } | validStatic
        "${CustomCompletionsProviderTest.canonicalName}.${validStatic.name}" || { CustomCompletionsProviderTest } | validStatic
    }

    def 'test that of function with #methodDeclaration throws with #message'() {
        when:
        CustomCompletionsProvider.of(this, methodDeclaration)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == message

        where:
        methodDeclaration                                                                || message
        'not.existing.Class.method'                                                      || 'Could not find class \'not.existing.Class\''
        "${CustomCompletionsProviderTest.canonicalName}.${valid.name}"                   ||
                "Invalid method '${valid.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
                "completions functions with class executor must be static"
        "${invalidReturnType.name}"                                                      ||
                "Invalid method '${invalidReturnType.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
                "must return an instance of ${Collection.canonicalName}"
        "${invalidReturnTypeStatic.name}"                                                ||
                "Invalid method '${invalidReturnTypeStatic.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
                "must return an instance of ${Collection.canonicalName}"
        "${CustomCompletionsProviderTest.canonicalName}.${invalidReturnTypeStatic.name}" ||
                "Invalid method '${invalidReturnTypeStatic.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
                "must return an instance of ${Collection.canonicalName}"
        'notFound'                                                                       ||
                "Could not find method 'notFound' in class '${CustomCompletionsProviderTest.canonicalName}'"
        'notFoundStatic'                                                                 ||
                "Could not find method 'notFoundStatic' in class '${CustomCompletionsProviderTest.canonicalName}'"
        "${CustomCompletionsProviderTest.canonicalName}.notFoundStatic"                  ||
                "Could not find method 'notFoundStatic' in class '${CustomCompletionsProviderTest.canonicalName}'"
    }

    List<String> valid() {
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
