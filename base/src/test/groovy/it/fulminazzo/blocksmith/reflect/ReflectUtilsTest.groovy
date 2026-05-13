//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package it.fulminazzo.blocksmith.reflect

import spock.lang.Specification

import java.lang.reflect.Type

class ReflectUtilsTest extends Specification {
    private static final Type LIST_PARAMETERIZED_TYPE = Mock.getDeclaredMethod('parameterizedType').genericReturnType
    private static final Type GENERIC_ARRAY_TYPE = Mock.getDeclaredMethod('genericArrayType').genericReturnType
    private static final Type NO_BOUNDS_WILDCARD_TYPE = Mock.getDeclaredMethod('wildcard').genericReturnType.actualTypeArguments[0] as Type
    private static final Type UPPER_BOUNDS_WILDCARD_TYPE = Mock.getDeclaredMethod('wildcardExtends').genericReturnType.actualTypeArguments[0] as Type
    private static final Type LOWER_BOUNDS_WILDCARD_TYPE = Mock.getDeclaredMethod('wildcardSuper').genericReturnType.actualTypeArguments[0] as Type
    private static final Type TYPE_VARIABLE = LOWER_BOUNDS_WILDCARD_TYPE.lowerBounds[0].actualTypeArguments[0] as Type

    def 'test that extendsType with #clazz, #type returns #expected'() {
        when:
        def actual = ReflectUtils.extendsType(clazz, type)

        then:
        actual == expected

        where:
        clazz   | type         || expected
        HashMap | HashMap      || true
        HashMap | AbstractMap  || true
        HashMap | Map          || true
        HashMap | AbstractList || false
        HashMap | List         || false
        HashMap | Collection   || false
        Cat     | Animal       || true
        Animal  | Cat          || false
        HashMap | Object       || true
        Object  | HashMap      || false
        Object  | Object       || true
    }

    def 'test that typeMatches with #type1 and #type2 returns #expected'() {
        when:
        def actual = ReflectUtils.typeMatches(type1, type2)

        then:
        actual == expected

        where:
        type1                                                              | type2                      || expected
        // Class
        Mock                                                               | Mock                       || true
        Mock                                                               | List                       || false
        List                                                               | Mock                       || false
        // ParameterizedType
        LIST_PARAMETERIZED_TYPE                                            | LIST_PARAMETERIZED_TYPE    || true
        LIST_PARAMETERIZED_TYPE                                            | List                       || true
        List                                                               | LIST_PARAMETERIZED_TYPE    || true
        Mock.getDeclaredMethod('otherParameterizedType').genericReturnType | LIST_PARAMETERIZED_TYPE    || false
        Mock.getDeclaredMethod('dualParameterizedType').genericReturnType  | LIST_PARAMETERIZED_TYPE    || false
        // TypeVariable
        Integer                                                            | TYPE_VARIABLE              || true
        Number                                                             | TYPE_VARIABLE              || false
        Comparable                                                         | TYPE_VARIABLE              || false
        Mock                                                               | TYPE_VARIABLE              || false
        // GenericArrayType
        Set[]                                                              | GENERIC_ARRAY_TYPE         || true
        Collection[]                                                       | GENERIC_ARRAY_TYPE         || false
        Set                                                                | GENERIC_ARRAY_TYPE         || false
        // WildcardType
        Object                                                             | NO_BOUNDS_WILDCARD_TYPE    || true
        Object                                                             | LOWER_BOUNDS_WILDCARD_TYPE || true
        Collection                                                         | LOWER_BOUNDS_WILDCARD_TYPE || true
        Object                                                             | UPPER_BOUNDS_WILDCARD_TYPE || false
        List                                                               | UPPER_BOUNDS_WILDCARD_TYPE || true
        LIST_PARAMETERIZED_TYPE                                            | UPPER_BOUNDS_WILDCARD_TYPE || true
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
        LIST_PARAMETERIZED_TYPE                                     || List
        GENERIC_ARRAY_TYPE                                          || Set[]
        Mock.getDeclaredMethod('wildcard').genericReturnType        || Collection
        NO_BOUNDS_WILDCARD_TYPE                                     || Object
        Mock.getDeclaredMethod('wildcardExtends').genericReturnType || Collection
        UPPER_BOUNDS_WILDCARD_TYPE                                  || List
        Mock.getDeclaredMethod('wildcardSuper').genericReturnType   || Collection
        LOWER_BOUNDS_WILDCARD_TYPE                                  || Collection
        TYPE_VARIABLE                                               || Number
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
        Mock.typeParameters[1]                                          || 'N'
        LIST_PARAMETERIZED_TYPE                                         || "${List.canonicalName}<${String.canonicalName}>"
        GENERIC_ARRAY_TYPE                                              || "${Set.canonicalName}<${Boolean.canonicalName}>[]"
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

    static class Animal {

    }

    static class Cat extends Animal {

    }

}
