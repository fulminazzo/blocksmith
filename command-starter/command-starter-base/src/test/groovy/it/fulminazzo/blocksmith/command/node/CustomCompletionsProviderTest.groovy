////file:noinspection unused
//TODO: update
//package it.fulminazzo.blocksmith.command.node
//
//import groovy.transform.EqualsAndHashCode
//import spock.lang.Specification
//
//import java.lang.reflect.Method
//import java.util.concurrent.CompletionException
//
//@EqualsAndHashCode
//class CustomCompletionsProviderTest extends Specification {
//    private static Method valid = CustomCompletionsProviderTest.getMethod('valid')
//    private static Method validStatic = CustomCompletionsProviderTest.getMethod('validStatic')
//    private static Method invalidReturnType = CustomCompletionsProviderTest.getMethod('invalidReturnType')
//    private static Method invalidReturnTypeStatic = CustomCompletionsProviderTest.getMethod('invalidReturnTypeStatic')
//
//    def 'test that getCompletions works'() {
//        given:
//        def provider = new CustomCompletionsProvider(this, valid)
//
//        when:
//        def actual = provider.completions
//
//        then:
//        actual == ['first', 'null', 'third']
//    }
//
//    def 'test that getCompletions throws Exception'() {
//        given:
//        def provider = new CustomCompletionsProvider(
//                this,
//                CustomCompletionsProviderTest.getMethod('exception')
//        )
//
//        when:
//        provider.completions
//
//        then:
//        def e = thrown(CompletionException)
//        def cause = e.cause
//        (cause.class == Exception)
//        cause.message == 'Test exception'
//    }
//
//    def 'test that getCompletions throws Runtime exception'() {
//        given:
//        def provider = new CustomCompletionsProvider(
//                this,
//                CustomCompletionsProviderTest.getMethod('runtimeException')
//        )
//
//        when:
//        provider.completions
//
//        then:
//        def e = thrown(RuntimeException)
//        e.message == 'Test runtime exception'
//    }
//
//    def 'test that getCompletions throws Illegal argument exception on private method'() {
//        given:
//        def provider = new CustomCompletionsProvider(
//                this,
//                CustomCompletionsProviderTest.getDeclaredMethod('privateMethod')
//        )
//
//        when:
//        provider.completions
//
//        then:
//        thrown(IllegalArgumentException)
//    }
//
//    def 'test that of function with #methodDeclaration returns #expectedRequester, #expectedMethod'() {
//        when:
//        def provider = CustomCompletionsProvider.of(this, methodDeclaration)
//
//        then:
//        provider.executor == expectedRequester()
//        provider.method == expectedMethod
//
//        where:
//        methodDeclaration                                                    || expectedRequester                 | expectedMethod
//        "${valid.name}"                                                      || { this }                          | valid
//        "${validStatic.name}"                                                || { CustomCompletionsProviderTest } | validStatic
//        "${CustomCompletionsProviderTest.canonicalName}.${validStatic.name}" || { CustomCompletionsProviderTest } | validStatic
//    }
//
//    def 'test that of function with #methodDeclaration throws with #message'() {
//        when:
//        CustomCompletionsProvider.of(this, methodDeclaration)
//
//        then:
//        def e = thrown(IllegalArgumentException)
//        e.message == message
//
//        where:
//        methodDeclaration                                                                || message
//        'not.existing.Class.method'                                                      || 'Could not find class \'not.existing.Class\''
//        "${CustomCompletionsProviderTest.canonicalName}.${valid.name}"                   ||
//                "Invalid method '${valid.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
//                "completions functions with class executor must be static"
//        "${invalidReturnType.name}"                                                      ||
//                "Invalid method '${invalidReturnType.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
//                "must return an instance of ${Collection.canonicalName}"
//        "${invalidReturnTypeStatic.name}"                                                ||
//                "Invalid method '${invalidReturnTypeStatic.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
//                "must return an instance of ${Collection.canonicalName}"
//        "${CustomCompletionsProviderTest.canonicalName}.${invalidReturnTypeStatic.name}" ||
//                "Invalid method '${invalidReturnTypeStatic.name}' in type '${CustomCompletionsProviderTest.canonicalName}': " +
//                "must return an instance of ${Collection.canonicalName}"
//        'notFound'                                                                       ||
//                "Could not find method 'notFound' in class '${CustomCompletionsProviderTest.canonicalName}'"
//        'notFoundStatic'                                                                 ||
//                "Could not find method 'notFoundStatic' in class '${CustomCompletionsProviderTest.canonicalName}'"
//        "${CustomCompletionsProviderTest.canonicalName}.notFoundStatic"                  ||
//                "Could not find method 'notFoundStatic' in class '${CustomCompletionsProviderTest.canonicalName}'"
//    }
//
//    List<String> valid() {
//        return ['first', null, 'third']
//    }
//
//    List<?> exception() {
//        throw new Exception('Test exception')
//    }
//
//    List<?> runtimeException() {
//        throw new RuntimeException('Test runtime exception')
//    }
//
//    private List<String> privateMethod() {
//        return []
//    }
//
//    String invalidReturnType() {
//        return ''
//    }
//
//    static List<String> validStatic() {
//        return []
//    }
//
//    static String invalidReturnTypeStatic() {
//        return ''
//    }
//
//}
