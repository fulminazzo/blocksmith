package it.fulminazzo.blocksmith.util

import spock.lang.Specification

class MapUtilsTest extends Specification {

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

}
