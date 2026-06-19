package it.fulminazzo.blocksmith.application

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode
import it.fulminazzo.blocksmith.application.node.RootLoaderNode
import spock.lang.Specification

class ApplicationInitializerTest extends Specification {
    private RootLoaderNode dependencyTree
    private FieldAnnotationNode first
    private FieldAnnotationNode second
    private FieldAnnotationNode third
    private FieldAnnotationNode fourth
    private FieldAnnotationNode fifth

    private ApplicationInitializer initializer

    void setup() {
        final application = new ValidApplication()

        dependencyTree = new RootLoaderNode()
        first = ApplicationTestUtils.newNode(application.class, 'first')
        second = ApplicationTestUtils.newNode(application.class, 'second')
        third = ApplicationTestUtils.newNode(application.class, 'third')
        fourth = ApplicationTestUtils.newNode(application.class, 'fourth')
        fifth = ApplicationTestUtils.newNode(application.class, 'fifth')

        dependencyTree.addChild(first)
        dependencyTree.addChild(fifth)

        first.addChild(second)
        first.addChild(third)

        third.addChild(fourth)

        initializer = Spy(ApplicationInitializer, constructorArgs : [application, dependencyTree])
    }

    def 'test that visitRootNode correctly visits tree with levels'() {
        when:
        initializer.visitRoot(dependencyTree)

        then:
        1 * initializer.visitField(first) >> {}
        1 * initializer.visitField(fifth) >> {}

        then:
        1 * initializer.visitField(second) >> {}
        1 * initializer.visitField(third) >> {}

        then:
        1 * initializer.visitField(fourth) >> {}
    }

    def 'test that getFieldValue of #fieldPath returns #expected'() {
        expect:
        ApplicationInitializer.getFieldValue(new ContainerClass(), fieldPath) == expected

        where:
        fieldPath                            || expected
        ''                                   || new ContainerClass()
        'field'                              || new ContainerClass.FieldClass()
        'field.name'                         || 'Alex'
        'field.ageData'                      || new ContainerClass.MethodClass()
        'field.ageData.information'          || new ContainerClass.NestedClass()
        'field.ageData.information.identity' || 'Batman'
        'method'                             || new ContainerClass.MethodClass()
        'method.age'                         || 10
        'method.information'                 || new ContainerClass.NestedClass()
        'method.information.identity'        || 'Batman'
    }

    def 'test that checkFieldInClass throws for #fieldPath'() {
        when:
        ApplicationInitializer.getFieldValue(new ContainerClass(), fieldPath)

        then:
        thrown(IllegalArgumentException)

        where:
        fieldPath << [
                'field.notFound',
                'method.invalid',
                'method.localizedName'
        ]
    }

}
