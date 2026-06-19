package it.fulminazzo.blocksmith.application.node

import it.fulminazzo.blocksmith.application.FieldAnnotationHandler
import it.fulminazzo.blocksmith.application.InvalidDependencies
import it.fulminazzo.blocksmith.application.MultipleDependencies
import it.fulminazzo.blocksmith.application.NoDependencies
import it.fulminazzo.blocksmith.application.SingleDependency
import spock.lang.Specification

import java.lang.annotation.Annotation
import java.lang.reflect.Field

class FieldAnnotationNodeTest extends Specification {
    private static final Annotation NO_DEPENDENCIES = new NoDependencies() {

        @Override
        Class<? extends Annotation> annotationType() {
            return NoDependencies
        }

    }
    private static final Annotation SINGLE_DEPENDENCY = new SingleDependency() {

        @Override
        String dependsOn() {
            return 'first'
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return SingleDependency
        }

    }
    private static final Annotation MULTIPLE_DEPENDENCIES_1 = new MultipleDependencies() {

        @Override
        String[] dependsOn() {
            return ['first']
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return MultipleDependencies
        }

    }
    private static final Annotation MULTIPLE_DEPENDENCIES_2 = new MultipleDependencies() {

        @Override
        String[] dependsOn() {
            return ['first', 'second']
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return MultipleDependencies
        }

    }
    private static final Annotation MULTIPLE_DEPENDENCIES_3 = new MultipleDependencies() {

        @Override
        String[] dependsOn() {
            return ['first', 'second', 'third.nested']
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return MultipleDependencies
        }

    }
    private static final Annotation INVALID_DEPENDENCIES = new InvalidDependencies() {

        @Override
        int[] dependsOn() {
            return [1, 2, 3]
        }

        @Override
        Class<? extends Annotation> annotationType() {
            return Annotation
        }

    }

    private final Field field = Mock(Field)
    private final FieldAnnotationHandler handler = Mock(FieldAnnotationHandler)

    def 'test that loadDependencies correctly stored nested fields'() {
        given:
        final annotation = MULTIPLE_DEPENDENCIES_3

        when:
        def node = new FieldAnnotationNode(annotation, field, handler)

        then:
        node.getFieldDependency('third') == 'nested'
    }

    def 'test that loadDependencies loads #annotation dependencies as #expected'() {
        when:
        def node = new FieldAnnotationNode(annotation, field, handler)

        then:
        node.fieldDependencies == expected.toSet()

        where:
        annotation              || expected
        NO_DEPENDENCIES         || []
        SINGLE_DEPENDENCY       || ['first']
        MULTIPLE_DEPENDENCIES_1 || ['first']
        MULTIPLE_DEPENDENCIES_2 || ['first', 'second']
        MULTIPLE_DEPENDENCIES_3 || ['first', 'second', 'third']
    }

    def 'test that loadDependencies of invalid dependsOn throws'() {
        when:
        new FieldAnnotationNode(INVALID_DEPENDENCIES, field, handler)

        then:
        def e = thrown(IllegalArgumentException)
        e.message =~ /.*int\[].*/
    }

}
