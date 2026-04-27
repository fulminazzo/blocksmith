package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class MigrationTest extends Specification {

    def 'test that migration methods work'() {
        given:
        def data = [
                'to_rename'           : true,
                'to_change'           : '',
                'to_update'           : 1.0,
                'to_remove'           : 'invalid value',
                'to_direct_update'    : [],
                'to_direct_update_map': ['Hello': 'world']
        ]

        when:
        def migration = new Migration(data)
                .remove('to_remove')
                .rename('to_rename', 'valid')
                .update('to_change', 'Hello, world!')
                .update('to_update', 'version', 2.0)
                .update('to_direct_update', o -> o.addAll([1, 2, 3]))
                .update('to_direct_update_map', 'map', o -> o.put('Goodbye', 'mars'))
                .add('name', 'blocksmith')

        and:
        def actual = migration.data

        then:
        actual == [
                'valid'           : true,
                'to_change'       : 'Hello, world!',
                'version'         : 2.0,
                'name'            : 'blocksmith',
                'to_direct_update': [1, 2, 3],
                'map'             : ['Hello': 'world', 'Goodbye': 'mars']
        ]
    }

    def 'test that #method with #data and #arguments throws'() {
        given:
        def migration = new Migration(data)

        when:
        migration."$method"(*arguments)

        then:
        thrown(IllegalArgumentException)

        where:
        data            | method   | arguments
        ['valid': true] | 'add'    | ['valid', false]
        [:]             | 'remove' | ['valid']
        [:]             | 'update' | ['valid', false]
        [:]             | 'update' | ['valid', 'not-valid', false]
        [:]             | 'update' | ['valid', (o) -> { }]
        [:]             | 'update' | ['valid', 'not-valid', (o) -> { }]
        [:]             | 'rename' | ['valid', 'not-valid']
    }

}
