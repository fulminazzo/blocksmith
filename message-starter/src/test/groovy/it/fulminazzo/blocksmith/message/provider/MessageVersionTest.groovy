package it.fulminazzo.blocksmith.message.provider

import spock.lang.Specification

class MessageVersionTest extends Specification {

    def 'test that migrate of #version and #messages returns updated messages'() {
        given:
        def expected = [
                'prefix' : 'Blocksmith',
                'general': ['greeting': '%prefix%Hello, %name%!'],
                'error'  : ['not-enough-arguments': '%prefix%You did not specify enough arguments']
        ]

        and:
        def reference = [
                'prefix' : 'Blocksmith',
                'general': ['greeting': '%prefix%Hello, %name%!'],
                'error'  : ['not-enough-arguments': '%prefix%You did not specify enough arguments']
        ]

        and:
        def messageVersion = MessageVersion.of(3.0)
                .migrate(1.0, m -> m.add('greeting')
                        .add('errors.arguments'))
                .migrate(2.0, m -> m.add('not-enough-arguments')
                        .add('prefix')
                        .update('greeting', 'general.greeting')
                        .remove('errors.arguments'))
                .migrate(3.0, m -> m
                        .rename('not-enough-arguments', 'error.not-enough-arguments')
                )

        when:
        def actual = messageVersion.applyMigrations(version, messages, reference)

        then:
        actual == expected

        where:
        version | messages
        0.0     | [:]
        1.0     | [
                'greeting': 'Hello, world!',
                'errors'  : ['arguments': 'Not enough arguments!']
        ]
        2.0     | [
                'prefix'              : 'Blocksmith',
                'general'             : ['greeting': '%prefix%Hello, %name%!'],
                'not-enough-arguments': '%prefix%You did not specify enough arguments'
        ]
        3.0     | [
                'prefix'                    : 'Blocksmith',
                'general'                   : ['greeting': '%prefix%Hello, %name%!'],
                'error.not-enough-arguments': '%prefix%You did not specify enough arguments'
        ]
    }

    def 'test that migrate of existing migration throws'() {
        given:
        def version = new MessageVersion(1.0).migrate(2.0, m -> m)

        when:
        version.migrate(2.0, m -> m)

        then:
        thrown(IllegalArgumentException)
    }

}
