package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class MapUtilsTest extends Specification {

    def 'test that stringify works'() {
        given:
        def map = [
                (null)      : 'invalid',
                'invalid'   : null,
                'object'    : 10,
                'collection': [
                        null,
                        'Hello, world!',
                        ['first', 'second'],
                        [['even', 'more'], ['nested', 'collection']]
                ],
                'map'       : [
                        'null'      : null,
                        'object'    : 10,
                        'collection': ['should', 'work', 'too'],
                        'nested'    : ['values': [1, 2, 3]]
                ]
        ]

        and:
        def expected = [
                'object'           : '10',
                'collection'       : 'Hello, world!\nfirst\nsecond\neven\nmore\nnested\ncollection',
                'map.object'       : '10',
                'map.collection'   : 'should\nwork\ntoo',
                'map.nested.values': '1\n2\n3'
        ]

        when:
        def actual = MapUtils.stringify(map)

        then:
        actual.sort() == expected.sort()
    }

    def 'test that flattening and unflattening of #map returns source map'() {
        when:
        def flattened = MapUtils.flatten(map)

        then:
        flattened == expected

        when:
        def unflattened = MapUtils.unflatten(flattened)

        then:
        if (expected.isEmpty()) assert unflattened.isEmpty()
        else unflattened == map

        where:
        map                                                                                             || expected
        [:]                                                                                             || [:]
        ['a': ['b': ['c': [:]]]]                                                                        || [:]
        ['Hello': 'world!']                                                                             || ['Hello': 'world!']
        ['players': ['Steve', 'Alex'], 'player': ['join': 'Welcome!', 'leave': 'Goodbye!']]             ||
                ['players': ['Steve', 'Alex'], 'player.join': 'Welcome!', 'player.leave': 'Goodbye!']
        ['a': ['b': ['c': ['d': 'deep']]]]                                                              || ['a.b.c.d': 'deep']
        ['server': ['host': 'localhost', 'port': ['http': 8080, 'https': 8443]]]                        ||
                ['server.host': 'localhost', 'server.port.http': 8080, 'server.port.https': 8443]
        ['db': ['hosts': ['node1', 'node2'], 'credentials': ['user': 'root', 'pass': 'secret']]]        ||
                ['db.hosts': ['node1', 'node2'], 'db.credentials.user': 'root', 'db.credentials.pass': 'secret']
        ['plugin': ['enable': true, 'debug': false], 'plugins': ['count': 3]]                           ||
                ['plugin.enable': true, 'plugin.debug': false, 'plugins.count': 3]
        ['level1': ['1': 'one', '2': 'two']]                                                            ||
                ['level1.1': 'one', 'level1.2': 'two']
        ['config': ['timeout': null, 'retries': 3]]                                                     ||
                ['config.timeout': null, 'config.retries': 3]
        ['foo': 'bar', 'baz': 'qux']                                                                    || ['foo': 'bar', 'baz': 'qux']
        ['world': ['regions': ['EU', 'US'], 'settings': ['lang': ['default': 'en', 'fallback': 'it']]]] ||
                ['world.regions': ['EU', 'US'], 'world.settings.lang.default': 'en', 'world.settings.lang.fallback': 'it']
    }

    def 'test that expandCollection works'() {
        given:
        def expanded = [:]

        and:
        def collection = [
                null,
                'Hello, world!',
                ['first', 'second'],
                [['even', 'more'], ['nested', 'collection']],
                [
                        'null'      : null,
                        'object'    : 10,
                        'collection': ['should', 'work', 'too'],
                        'nested'    : ['values': [1, 2, 3]]
                ]
        ]

        and:
        def expected = [
                'list[0]'      : null,
                'list[1]'      : 'Hello, world!',
                'list[2][0]'   : 'first',
                'list[2][1]'   : 'second',
                'list[3][0][0]': 'even',
                'list[3][0][1]': 'more',
                'list[3][1][0]': 'nested',
                'list[3][1][1]': 'collection',
                'list[4]'      : [
                        'null'         : null,
                        'object'       : 10,
                        'collection[0]': 'should',
                        'collection[1]': 'work',
                        'collection[2]': 'too',
                        'nested'       : [
                                'values[0]': 1,
                                'values[1]': 2,
                                'values[2]': 3
                        ]
                ]
        ]

        when:
        MapUtils.expandCollection(expanded, collection, 'list')

        then:
        expanded == expected
    }

    def 'test that convertArray converts #object to #expected'() {
        when:
        def actual = MapUtils.convertArray(object)

        then:
        actual == expected

        where:
        object                                                                       || expected
        null                                                                         || null
        [:]                                                                          || [:]
        (0..9).collect { it as byte }.toArray(new byte[10])                          || (0..9).collect { it as byte }
        (0..9).collect { it as short }.toArray(new short[10])                        || (0..9).collect { it as short }
        (0..9).collect { it as int }.toArray(new int[10])                            || (0..9).collect { it as int }
        (0..9).collect { it as long }.toArray(new long[10])                          || (0..9).collect { it as long }
        (0..9).collect { it as float }.toArray(new float[10])                        || (0..9).collect { it as float }
        (0..9).collect { it as double }.toArray(new double[10])                      || (0..9).collect { it as double }
        ('a'..'z').collect { it as char }.toArray(new char[26])                      || ('a'..'z').collect { it as char }
        [true, false].collect { it as boolean }.toArray(new boolean[2])              || [true, false].collect { it as boolean }
        ['Hello', 'world'].toArray(new String[2])                                    || ['Hello', 'world']
        ['nested', ['Hello', 'world'].toArray(new String[2])].toArray(new Object[2]) || ['nested', ['Hello', 'world']]
    }

}
