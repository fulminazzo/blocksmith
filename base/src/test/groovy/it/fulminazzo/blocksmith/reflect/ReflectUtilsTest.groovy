//file:noinspection unused
package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.Type

class ReflectUtilsTest extends Specification {
    private static final Type listParameterizedType = Mock.getDeclaredMethod('parameterizedType').genericReturnType
    private static final Type genericArrayType = Mock.getDeclaredMethod('genericArrayType').genericReturnType
    private static final Type noBoundsWildcardType = Mock.getDeclaredMethod('wildcard').genericReturnType.actualTypeArguments[0]
    private static final Type upperBoundsWildcardType = Mock.getDeclaredMethod('wildcardExtends').genericReturnType.actualTypeArguments[0]
    private static final Type lowerBoundsWildcardType = Mock.getDeclaredMethod('wildcardSuper').genericReturnType.actualTypeArguments[0]
    private static final Type typeVariable = lowerBoundsWildcardType.lowerBounds[0].actualTypeArguments[0]

    def 'test that toClass of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toClass(type)

        then:
        actual == expected

        where:
        type                                                        || expected
        Mock                                                        || Mock
        listParameterizedType                                       || List
        genericArrayType                                            || Set[]
        Mock.getDeclaredMethod('wildcard').genericReturnType        || Collection
        noBoundsWildcardType                                        || Object
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType || Collection
        upperBoundsWildcardType                                     || List
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType   || Collection
        lowerBoundsWildcardType                                     || Collection
        typeVariable                                                || Number
    }

    def 'test that toString of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toString(type)

        then:
        actual == expected

        where:
        type                                                        || expected
        Mock                                                        || "${Mock.canonicalName}"
        listParameterizedType                                       || "${List.canonicalName}<${String.canonicalName}>"
        genericArrayType                                            || "${Set.canonicalName}<${Boolean.canonicalName}>[]"
        Mock.getDeclaredMethod('wildcard').genericReturnType        || "${Collection.canonicalName}<?>"
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType || "${Collection.canonicalName}<? extends ${List.canonicalName}<${String.canonicalName}>>"
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType   || "${Collection.canonicalName}<? super ${Collection.canonicalName}<T extends ${Number.canonicalName} & ${Comparable.canonicalName}>>"
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
