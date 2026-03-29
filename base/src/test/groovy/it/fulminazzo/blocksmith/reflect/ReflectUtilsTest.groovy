//file:noinspection unused
package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

class ReflectUtilsTest extends Specification {

    def 'test that toClass of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toClass(type)

        then:
        actual == expected

        where:
        type                                                                                                                   || expected
        Mock                                                                                                                   || Mock
        Mock.getDeclaredMethod('parameterizedType').genericReturnType                                                          || List
        Mock.getDeclaredMethod('genericArrayType').genericReturnType                                                           || Set[]
        Mock.getDeclaredMethod('wildcard').genericReturnType                                                                   || Collection
        Mock.getDeclaredMethod('wildcard').genericReturnType.actualTypeArguments[0]                                            || Object
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType                                                            || Collection
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType.actualTypeArguments[0]                                     || List
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType                                                              || Collection
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType.actualTypeArguments[0]                                       || Collection
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType.actualTypeArguments[0].lowerBounds[0].actualTypeArguments[0] || Number
    }

    def 'test that toString of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toString(type)

        then:
        actual == expected

        where:
        type                                                          || expected
        Mock                                                          || "${Mock.canonicalName}"
        Mock.getDeclaredMethod('parameterizedType').genericReturnType || "${List.canonicalName}<${String.canonicalName}>"
        Mock.getDeclaredMethod('genericArrayType').genericReturnType  || "${Set.canonicalName}<${Boolean.canonicalName}>[]"
        Mock.getDeclaredMethod('wildcard').genericReturnType          || "${Collection.canonicalName}<?>"
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType   || "${Collection.canonicalName}<? extends ${List.canonicalName}<${String.canonicalName}>>"
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType     || "${Collection.canonicalName}<? super ${Collection.canonicalName}<T extends ${Number.canonicalName} & ${Comparable.canonicalName}>>"
    }

    static class Mock<T extends Number & Comparable> {

        static List<String> parameterizedType() {
            throw new UnsupportedOperationException()
        }

        static Set<Boolean>[] genericArrayType() {
            throw new UnsupportedOperationException()
        }

        Collection<?> wildcard() {
            throw new UnsupportedOperationException()
        }

        Collection<? extends List<String>> wildcardExtends() {
            throw new UnsupportedOperationException()
        }

        Collection<? super Collection<T>> wildcardSuper() {
            throw new UnsupportedOperationException()
        }

    }

}
