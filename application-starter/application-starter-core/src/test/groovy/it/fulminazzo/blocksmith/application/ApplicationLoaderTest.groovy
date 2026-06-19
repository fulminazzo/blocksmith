//file:noinspection unused
//file:noinspection GrMethodMayBeStatic
package it.fulminazzo.blocksmith.application

import it.fulminazzo.blocksmith.application.node.FieldAnnotationNode
import it.fulminazzo.blocksmith.application.node.RootLoaderNode
import it.fulminazzo.blocksmith.reflect.Reflect
import spock.lang.Specification

class ApplicationLoaderTest extends Specification {
    private final ApplicationLoader loader = new ApplicationLoader(new ValidApplication())

    void setupSpec() {
        ApplicationTestUtils.loadHandlers()
    }

    def 'test that load works'() {
        when:
        def node = loader.load()

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
        def node1 = ApplicationTestUtils.newNode(ValidApplication, 'first')
        def node2 = ApplicationTestUtils.newNode(ValidApplication, 'second')

        and:
        node.addChild(node1)
        node.addChild(node2)
        node1.addChild(node2)
        node2.addChild(node1)

        when:
        loader.validateTree(node)

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.*Circular dependency.+${ValidApplication.canonicalName}.*/
    }

    def 'test that buildDependencyTree correctly builds inheritance between nodes'() {
        when:
        def node = loader.buildDependencyTree(
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
        def node1 = ApplicationTestUtils.newNode(ValidApplication, 'first')
        Reflect.on(node1).set('dependencies', ['second' : ''])

        and:
        def node2 = ApplicationTestUtils.newNode(ValidApplication, 'second')
        Reflect.on(node2).set('dependencies', ['first' : ''])

        when:
        loader.buildDependencyTree(['first' : node1, 'second' : node2])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.*Circular dependency.+${ValidApplication.canonicalName}.*/
    }

    def 'test that validateNodeDependencies does not throw for valid nodes'() {
        when:
        loader.validateNodesDependencies(
                validNodes.collectEntries { [it.fieldName, it] }
        )

        then:
        noExceptionThrown()
    }

    def 'test that validateNodesDependencies throws for subfield not found'() {
        given:
        def node1 = ApplicationTestUtils.newNode(ValidApplication, 'first')
        Reflect.on(node1).set('dependencies', ['second' : 'nested'])

        and:
        def node2 = ApplicationTestUtils.newNode(ValidApplication, 'second')

        when:
        loader.validateNodesDependencies(['first' : node1, 'second' : node2])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.* subfield.+nested.+second.*/
    }

    def 'test that validateNodesDependencies throws for field not found'() {
        given:
        def node = ApplicationTestUtils.newNode(ValidApplication, 'first')
        Reflect.on(node).set('dependencies', ['invalid' : ''])

        when:
        loader.validateNodesDependencies(['node' : node])

        then:
        def e = thrown(ApplicationLoadingException)
        e.message =~ /.* field.+invalid.*/
    }

    def 'test that loadFieldAnnotationNodes correctly returns all nodes'() {
        when:
        def nodes = loader.loadFieldAnnotationNodes()

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
                ApplicationTestUtils.newNode(ValidApplication, 'first'),
                ApplicationTestUtils.newNode(ValidApplication, 'second'),
                ApplicationTestUtils.newNode(ValidApplication, 'third'),
                ApplicationTestUtils.newNode(ValidApplication, 'fourth'),
                ApplicationTestUtils.newNode(ValidApplication, 'fifth')
        ]
    }

}
