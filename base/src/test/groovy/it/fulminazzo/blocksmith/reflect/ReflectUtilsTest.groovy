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

    def 'test that extendsType with #clazz, #type returns #expected'() {
        when:
        def actual = ReflectUtils.extendsType(clazz, type)

        then:
        actual == expected

        where:
        clazz       | type         || expected
        HashMap     | HashMap      || true
        HashMap     | AbstractMap  || true
        HashMap     | Map          || true
        HashMap     | AbstractList || false
        HashMap     | List         || false
        HashMap     | Collection   || false
        Person      | NamedEntity  || true
        NamedEntity | Person       || false
        HashMap     | Object       || true
        Object      | HashMap      || false
        Object      | Object       || true
    }

    def 'test that typeMatches with #type1 and #type2 returns #expected'() {
        when:
        def actual = ReflectUtils.typeMatches(type1, type2)

        then:
        actual == expected

        where:
        type1                                                              | type2                   || expected
        // Class
        Mock                                                               | Mock                    || true
        Mock                                                               | List                    || false
        List                                                               | Mock                    || false
        // ParameterizedType
        listParameterizedType                                              | listParameterizedType   || true
        listParameterizedType                                              | List                    || true
        List                                                               | listParameterizedType   || true
        Mock.getDeclaredMethod('otherParameterizedType').genericReturnType | listParameterizedType   || false
        Mock.getDeclaredMethod('dualParameterizedType').genericReturnType  | listParameterizedType   || false
        // TypeVariable
        Integer                                                            | typeVariable            || true
        Number                                                             | typeVariable            || false
        Comparable                                                         | typeVariable            || false
        Mock                                                               | typeVariable            || false
        // GenericArrayType
        Set[]                                                              | genericArrayType        || true
        Collection[]                                                       | genericArrayType        || false
        Set                                                                | genericArrayType        || false
        // WildcardType
        Object                                                             | noBoundsWildcardType    || true
        Object                                                             | lowerBoundsWildcardType || true
        Collection                                                         | lowerBoundsWildcardType || true
        Object                                                             | upperBoundsWildcardType || false
        List                                                               | upperBoundsWildcardType || true
        listParameterizedType                                              | upperBoundsWildcardType || true
    }

    def 'test that typeMatches does not throw for unknown type'() {
        expect:
        !ReflectUtils.typeMatches(String, Mock(Type))
    }

    def 'test that toClass of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toClass(type)

        then:
        actual == expected

        where:
        type                                                        || expected
        Mock                                                        || Mock
        Mock.typeParameters[0]                                      || Number
        Mock.typeParameters[1]                                      || Object
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

    def 'test that toClass does not throws for unknown type'() {
        when:
        ReflectUtils.toClass(Mock(Type))

        then:
        thrown(IllegalArgumentException)
    }

    def 'test that toString of #type returns #expected'() {
        when:
        def actual = ReflectUtils.toString(type)

        then:
        actual == expected

        where:
        type                                                            || expected
        Mock                                                            || "${Mock.canonicalName}"
        Mock.typeParameters[0]                                          || "T extends ${Number.canonicalName} & ${Comparable.canonicalName}"
        Mock.typeParameters[1]                                          || "N"
        listParameterizedType                                           || "${List.canonicalName}<${String.canonicalName}>"
        genericArrayType                                                || "${Set.canonicalName}<${Boolean.canonicalName}>[]"
        Mock.getDeclaredMethod('wildcard').genericReturnType            || "${Collection.canonicalName}<?>"
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType     || "${Collection.canonicalName}<? extends ${List.canonicalName}<${String.canonicalName}>>"
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType       || "${Collection.canonicalName}<? super ${Collection.canonicalName}<T extends ${Number.canonicalName} & ${Comparable.canonicalName}>>"
        Mock.getDeclaredMethod('wildcardSuperObject').genericReturnType || "${Collection.canonicalName}<?>"
    }

    def 'test that toString does not throws for unknown type'() {
        when:
        ReflectUtils.toString(Mock(Type))

        then:
        thrown(IllegalArgumentException)
    }

    static class Mock<T extends Number & Comparable, N> {

        static List<String> parameterizedType() {
            throw new UnsupportedOperationException()
        }

        static List<Object> otherParameterizedType() {
            throw new UnsupportedOperationException()
        }

        static Map<String, List> dualParameterizedType() {
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

        Collection<? super Object> wildcardSuperObject() {
            throw new UnsupportedOperationException()
        }

    }

}
