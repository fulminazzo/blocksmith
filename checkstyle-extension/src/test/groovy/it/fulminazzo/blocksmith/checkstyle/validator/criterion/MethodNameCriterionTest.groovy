package it.fulminazzo.blocksmith.checkstyle.validator.criterion

import com.puppycrawl.tools.checkstyle.api.DetailAST
import org.mockito.Mockito
import spock.lang.Specification

class MethodNameCriterionTest extends Specification {

    def 'test that #criterion returns #expected for #name and #message'() {
        given:
        def node = Mock(DetailAST)

        and:
        def mock = Mockito.mockStatic(CriterionUtils)
        mock.when { CriterionUtils.getMethodName(node) }.thenReturn(name)

        expect:
        criterion.matches(node) == expected

        and:
        criterion.errorMessage == message

        cleanup:
        mock.close()

        where:
        criterion                             | name       || expected | message
        // Any
        MethodNameCriterion.ANY               | 'print'    || true     | 'method.name.any'
        MethodNameCriterion.ANY               | 'get'      || false    | 'method.name.any'
        MethodNameCriterion.ANY               | 'is'       || false    | 'method.name.any'
        MethodNameCriterion.ANY               | 'set'      || false    | 'method.name.any'
        MethodNameCriterion.ANY               | 'equals'   || false    | 'method.name.any'
        MethodNameCriterion.ANY               | 'hashCode' || false    | 'method.name.any'
        MethodNameCriterion.ANY               | 'toString' || false    | 'method.name.any'
        // Getter and setter
        MethodNameCriterion.GETTER_AND_SETTER | 'print'    || false    | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'get'      || true     | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'is'       || true     | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'set'      || true     | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'equals'   || false    | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'hashCode' || false    | 'method.name.getterAndSetter'
        MethodNameCriterion.GETTER_AND_SETTER | 'toString' || false    | 'method.name.getterAndSetter'
        // Equals
        MethodNameCriterion.EQUALS            | 'print'    || false    | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'get'      || false    | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'is'       || false    | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'set'      || false    | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'equals'   || true     | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'hashCode' || false    | 'method.name.equals'
        MethodNameCriterion.EQUALS            | 'toString' || false    | 'method.name.equals'
        // HashCode
        MethodNameCriterion.HASH_CODE         | 'print'    || false    | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'get'      || false    | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'is'       || false    | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'set'      || false    | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'equals'   || false    | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'hashCode' || true     | 'method.name.hashCode'
        MethodNameCriterion.HASH_CODE         | 'toString' || false    | 'method.name.hashCode'
        // ToString
        MethodNameCriterion.TO_STRING         | 'print'    || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'get'      || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'is'       || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'set'      || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'equals'   || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'hashCode' || false    | 'method.name.toString'
        MethodNameCriterion.TO_STRING         | 'toString' || true     | 'method.name.toString'
    }

}
