package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class BeanConfigurationBuilderTest extends Specification {

    private BeanConfigurationBuilder builder

    void setup() {
        builder = new BeanConfigurationBuilder([:], [:])
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
