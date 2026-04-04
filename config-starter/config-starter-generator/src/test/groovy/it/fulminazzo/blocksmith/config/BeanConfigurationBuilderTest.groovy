package it.fulminazzo.blocksmith.config

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import spock.lang.Specification

class BeanConfigurationBuilderTest extends Specification {

    private BeanConfigurationBuilder builder

    void setup() {
        builder = new BeanConfigurationBuilder([:], [:])
    }

    def 'test that parseProperty of existing field and getter correctly updates nodes'() {
        given:
        def key = new CommentKey('object', ['Hello, world!'])

        and:
        def f = new FieldDeclaration()
                .setPublic(true)
                .setAllTypes(StaticJavaParser.parseType('double'))
                .addSingleMemberAnnotation('Annotation', 'true')
        f.addVariable(new VariableDeclarator(StaticJavaParser.parseType('Boolean'), 'object'))
        builder.fields['object'] = f
        def method = new MethodDeclaration()
                .setName('getObject')
                .setProtected(true)
                .setFinal(true)
                .setType(StaticJavaParser.parseType('double'))
        method.createBody().addStatement(
                StaticJavaParser.parseStatement('throw new UnsupportedOperationException();')
        )
        builder.methods['getObject'] = method
        method = new MethodDeclaration()
                .setName('setObject')
                .setPrivate(true)
                .setAbstract(true)
                .setType(StaticJavaParser.parseType('String'))
        method.addParameter('String', 'value')
        method.createBody().addStatement(
                StaticJavaParser.parseStatement('throw new UnsupportedOperationException();')
        )
        builder.methods['setObject'] = method

        when:
        builder.parseProperty(key, value)

        then:
        def field = builder.fields['object']
        field != null
        field.toString() == "@Annotation(true)\n" +
                "@Comment(\"Hello, world!\")\n" +
                "public $type object = ${builder.getInitializer(value)};"

        and:
        def getter = builder.methods['getObject']
        getter != null
        getter.toString() == "protected final $type getObject() {\n" +
                "    throw new UnsupportedOperationException();\n" +
                "}"

        and:
        def setter = builder.methods['setObject']
        setter != null
        setter.toString() == "private void setObject(final $type object) {\n" +
                "    throw new UnsupportedOperationException();\n" +
                "}"

        where:
        value                                      | type
        null                                       | 'Object'
        10                                         | 'Integer'
        'Goodbye, mars!'                           | 'String'
        ['Goodbye', 'mars']                        | 'List<String>'
        ['Goodbye', 'mars'].toSet()                | 'Set<String>'
        [['Hello', 'world'], ['Goodbye', 'mars!']] | 'List<ArrayList<String>>'
    }

    def 'test that parseProperty of non-existing field and getter correctly creates nodes'() {
        given:
        def key = new CommentKey('object', ['Hello, world!'])

        when:
        builder.parseProperty(key, value)

        then:
        def field = builder.fields['object']
        field != null
        field.toString() == "@Comment(\"Hello, world!\")\n" +
                "private $type object = ${builder.getInitializer(value)};"

        and:
        def getter = builder.methods['getObject']
        getter != null
        getter.toString() == "public $type getObject() {\n" +
                "    return object;\n" +
                "}"

        and:
        def setter = builder.methods['setObject']
        setter != null
        setter.toString() == "public void setObject(final $type object) {\n" +
                "    this.object = object;\n" +
                "}"

        where:
        value                                      | type
        null                                       | 'Object'
        10                                         | 'Integer'
        'Goodbye, mars!'                           | 'String'
        ['Goodbye', 'mars']                        | 'List<String>'
        ['Goodbye', 'mars'].toSet()                | 'Set<String>'
        [['Hello', 'world'], ['Goodbye', 'mars!']] | 'List<ArrayList<String>>'
    }

    def 'test that convertComments correctly converts #key'() {
        when:
        builder.convertComments(key, field)

        and:
        def annotation = field.getAnnotationByClass(Comment)
                .map { it.asSingleMemberAnnotationExpr() }

        then:
        if (expected instanceof String) {
            assert annotation.isPresent()
            assert annotation.get().toString() == expected
        } else assert !annotation.isPresent()

        where:
        key                                        | field                  || expected
        new CommentKey('key', [])                  | new FieldDeclaration() || null
        new CommentKey('key', ['Hello, world!'])   | new FieldDeclaration() || '@Comment("Hello, world!")'
        new CommentKey('key', ['Hello', 'world!']) | new FieldDeclaration() || '@Comment({ "Hello", "world!" })'
        new CommentKey('key', [])                  | new FieldDeclaration().addSingleMemberAnnotation(Comment, '"Goodbye, mars!"')
                                                                            || null
        new CommentKey('key', ['Hello, world!'])   | new FieldDeclaration().addSingleMemberAnnotation(Comment, '"Goodbye, mars!"')
                                                                            || '@Comment("Hello, world!")'
        new CommentKey('key', ['Hello', 'world!']) | new FieldDeclaration().addSingleMemberAnnotation(Comment, '"Goodbye, mars!"')
                                                                            || '@Comment({ "Hello", "world!" })'
    }

    def 'test that getInitializer of collection #collection returns #expected and adds #imports'() {
        when:
        def actual = builder.getInitializer(collection)

        then:
        actual == expected

        and:
        builder.imports.containsKey(Arrays.canonicalName)

        and:
        imports.forEach {
            assert builder.imports.containsKey(it.canonicalName)
        }

        where:
        collection                           || expected                                   | imports
        new ArrayList<>([1, 2, 3])           || 'new ArrayList<>(Arrays.asList(1, 2, 3))'  | [ArrayList]
        new LinkedList<>([1, 2, 3])          || 'new LinkedList<>(Arrays.asList(1, 2, 3))' | [LinkedList]
        new HashSet<>([1, 2, 3])             || 'new HashSet<>(Arrays.asList(1, 2, 3))'    | [HashSet]
        new TreeSet<>([1, 2, 3])             || 'new TreeSet<>(Arrays.asList(1, 2, 3))'    | [TreeSet]
        new ArrayList<>([
                new LinkedList<>([1, 2, 3]),
                new HashSet<>([4, 5, 6])
        ])                                   || 'new ArrayList<>(Arrays.asList(' +
                'new LinkedList<>(Arrays.asList(1, 2, 3)), ' +
                'new HashSet<>(Arrays.asList(4, 5, 6))' +
                '))'                                                                       | [ArrayList, LinkedList, HashSet]
    }

    def 'test that getInitializer of #object returns #expected and adds no import'() {
        when:
        def actual = builder.getInitializer(object)

        then:
        actual == expected

        and:
        builder.imports.isEmpty()

        where:
        object           || expected
        null             || 'null'
        1 as byte        || '1'
        1 as Byte        || '1'
        1 as short       || '1'
        1 as Short       || '1'
        1 as int         || '1'
        1 as Integer     || '1'
        1 as long        || '1'
        1 as Long        || '1'
        1 as float       || '1.0'
        1 as Float       || '1.0'
        1 as double      || '1.0'
        1 as Double      || '1.0'
        'a' as char      || '\'a\''
        'a' as Character || '\'a\''
        'Hello, world!'  || '"Hello, world!"'
    }

}
