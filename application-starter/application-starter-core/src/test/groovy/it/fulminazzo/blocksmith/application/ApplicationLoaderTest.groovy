//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package it.fulminazzo.blocksmith.application

import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class ApplicationLoaderTest extends Specification {
    private final ApplicationLoader validLoader = new ApplicationLoader(new ValidApplication())

    void setupSpec() {
        ApplicationHandlers.registerFieldAnnotationHandler(NoDependencies, {})
        ApplicationHandlers.registerFieldAnnotationHandler(SingleDependency, {})
        ApplicationHandlers.registerFieldAnnotationHandler(MultipleDependencies, {})
    }

    def 'test that load works'() {
        when:
        def node = validLoader.load()

        then:
        def rootChildren = node.children
        rootChildren.size() == 2

        and:
        def first = rootChildren[0]
        def firstChildren = first.children
        firstChildren.size() == 2

        and:
        def second = firstChildren[0]
        def secondChildren = second.children
        secondChildren.size() == 1

        and:
        def third = firstChildren[0]
        def thirdChildren = third.children
        thirdChildren.size() == 1

        and:
        def fourth = secondChildren[0]
        fourth == thirdChildren[0]
        def fourthChildren = fourth.children
        fourthChildren.size() == 0

        and:
        def fifth = rootChildren[1]
        def fifthChildren = fifth.children
        fifthChildren.size() == 0
    }

    def 'test that validateTree throws InvalidApplicationException for circular dependency'() {
        given:
        def node = new RootLoaderNode()
        def node1 = newNode(ValidApplication, 'first')
        def node2 = newNode(ValidApplication, 'second')

        and:
        node.addChild(node1)
        node.addChild(node2)
        node1.addChild(node2)
        node2.addChild(node1)

        when:
        validLoader.validateTree(node)

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.*Circular dependency.+${ValidApplication.canonicalName}.*/
    }

    def 'test that buildDependencyTree correctly builds inheritance between nodes'() {
        when:
        def node = validLoader.buildDependencyTree(
                validNodes.collectEntries { [it.fieldName, it] }
        )

        then:
        def rootChildren = node.children
        rootChildren.size() == 2

        and:
        def first = rootChildren[0]
        def firstChildren = first.children
        firstChildren.size() == 2

        and:
        def second = firstChildren[0]
        def secondChildren = second.children
        secondChildren.size() == 1

        and:
        def third = firstChildren[0]
        def thirdChildren = third.children
        thirdChildren.size() == 1

        and:
        def fourth = secondChildren[0]
        fourth == thirdChildren[0]
        def fourthChildren = fourth.children
        fourthChildren.size() == 0

        and:
        def fifth = rootChildren[1]
        def fifthChildren = fifth.children
        fifthChildren.size() == 0
    }

    def 'test that buildDependencyTree throws InvalidApplicationException if no dependency is child of root node'() {
        given:
        def node1 = newNode(ValidApplication, 'first')
        Reflect.on(node1).set('dependencies', ['second' : ''])

        and:
        def node2 = newNode(ValidApplication, 'second')
        Reflect.on(node2).set('dependencies', ['first' : ''])

        when:
        validLoader.buildDependencyTree(['first' : node1, 'second' : node2])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.*Circular dependency.+${ValidApplication.canonicalName}.*/
    }

    def 'test that validateNodeDependencies does not throw for valid nodes'() {
        when:
        validLoader.validateNodesDependencies(
                validNodes.collectEntries { [it.fieldName, it] }
        )

        then:
        noExceptionThrown()
    }

    def 'test that validateNodesDependencies throws for subfield not found'() {
        given:
        def node1 = newNode(ValidApplication, 'first')
        Reflect.on(node1).set('dependencies', ['second' : 'nested'])

        and:
        def node2 = newNode(ValidApplication, 'second')

        when:
        validLoader.validateNodesDependencies(['first' : node1, 'second' : node2])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.* subfield.+nested.+second.*/
    }

    def 'test that validateNodesDependencies throws for field not found'() {
        given:
        def node = newNode(ValidApplication, 'first')
        Reflect.on(node).set('dependencies', ['invalid' : ''])

        when:
        validLoader.validateNodesDependencies(['node' : node])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.* field.+invalid.*/
    }

    def 'test that loadFieldAnnotationNodes correctly returns all nodes'() {
        when:
        def nodes = validLoader.loadFieldAnnotationNodes()

        then:
        nodes == validNodes
    }

    def 'test that checkFieldInClass of #fieldPath does not throw'() {
        expect:
        ApplicationLoader.checkFieldInClass(ContainerClass, fieldPath)

        where:
        fieldPath << [
                'field',
                'field.name',
                'field.ageData',
                'field.ageData.information',
                'field.ageData.information.identity',
                'method',
                'method.age',
                'method.information',
                'method.information.identity',
        ]
    }

    def 'test that checkFieldInClass throws for #fieldPath'() {
        expect:
        !ApplicationLoader.checkFieldInClass(ContainerClass, fieldPath)

        where:
        fieldPath << [
                'field.notFound',
                'method.invalid',
                'method.localizedName'
        ]
    }

    protected static List<FieldAnnotationNode> getValidNodes() {
        return [
                newNode(ValidApplication, 'first'),
                newNode(ValidApplication, 'second'),
                newNode(ValidApplication, 'third'),
                newNode(ValidApplication, 'fourth'),
                newNode(ValidApplication, 'fifth')
        ]
    }

    protected static FieldAnnotationNode newNode(
            final Class<? extends Application> applicationClass,
            final String fieldName
    ) {
        def field = applicationClass.getDeclaredField(fieldName)
        def annotation = field.annotations[0]
        return new FieldAnnotationNode(
                annotation,
                field,
                ApplicationHandlers.getFieldAnnotationHandler(annotation.annotationType()).orElseThrow()
        )
    }

    private static final class ContainerClass {

        private final FieldClass field = new FieldClass()

        private final MethodClass method = new MethodClass()

    }

    private static final class FieldClass {

        String name = 'Alex'

        MethodClass ageData = new MethodClass()

    }

    private static final class MethodClass {

        int getAge() {
            return 10
        }

        void setAge(int age) {

        }

        NestedClass getInformation() {
            return new NestedClass()
        }

        String getLocalizedName(final String name) {
            throw new UnsupportedOperationException()
        }

    }

    private static final class NestedClass {

        String identity = 'Batman'

    }

}
