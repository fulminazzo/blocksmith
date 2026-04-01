package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class MigrationTest extends Specification {

    def 'test that migration methods work'() {
        given:
        def data = [
                'to_rename': true,
                'to_change': '',
                'to_update': 1.0,
                'to_remove': 'invalid value'
        ]

        when:
        def migration = new Migration(data)
                .remove('to_remove')
                .rename('to_rename', 'valid')
                .update('to_change', 'Hello, world!')
                .update('to_update', 'version', 2.0)
                .add('name', 'blocksmith')

        and:
        def actual = migration.data

        then:
        actual == [
                'valid'    : true,
                'to_change': 'Hello, world!',
                'version'  : 2.0,
                'name'     : 'blocksmith'
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
        [:]             | 'rename' | ['valid', 'not-valid']
    }

}
