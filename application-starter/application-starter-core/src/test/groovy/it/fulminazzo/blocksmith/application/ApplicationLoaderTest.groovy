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

    def 'test that validateTree throws InvalidApplicationException for circular dependencies'() {
        given:
        def node = new LoaderNode()
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
        def e = thrown(InvalidApplicationException)
        e.message =~ /.*Circular.+${ValidApplication.canonicalName}.*/
    }

    def 'test that buildDependencyTree correctly builds inheritance between nodes'() {
        when:
        def node = validLoader.buildDependencyTree(validNodes)

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

    def 'test that buildDependencyTree throws InvalidApplicationException if a dependency is not found'() {
        given:
        def node = newNode(ValidApplication, 'first')
        Reflect.on(node).set('dependencies', ['invalid'].toSet())

        when:
        validLoader.buildDependencyTree([node])

        then:
        def e = thrown(InvalidApplicationException)
        e.message =~ /.*${ValidApplication.canonicalName}.+${NoDependencies.canonicalName}.+first.+invalid.*/
    }

    def 'test that buildDependencyTree throws InvalidApplicationException if no dependency is child of root node'() {
        given:
        def node1 = newNode(ValidApplication, 'first')
        Reflect.on(node1).set('dependencies', ['second'].toSet())

        and:
        def node2 = newNode(ValidApplication, 'second')
        Reflect.on(node2).set('dependencies', ['first'].toSet())

        when:
        validLoader.buildDependencyTree([node1, node2])

        then:
        def e = thrown(InvalidApplicationException)
        e.message =~ /.*Circular.+${ValidApplication.canonicalName}.*/
    }

    def 'test that loadFieldAnnotationNodes correctly returns all nodes'() {
        when:
        def nodes = validLoader.loadFieldAnnotationNodes()

        then:
        nodes == validNodes
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

}
