package it.fulminazzo.blocksmith.message.provider

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import spock.lang.Specification

@Slf4j
class MessageProviderMigrationTest extends Specification {

    def 'test that resource correctly handles migrations from #messages'() {
        given:
        final workDir = new File('build/resources/test/message_provider_migrations')
        final resource = 'messages-version.yml'
        ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML).store(workDir, resource.split('\\.')[0], messages)

        and:
        def version = MessageVersion.of(3.0)
                .migrate(2.0, m -> m.add('greeting')
                        .update('not-enough-arguments', 'error.not-enough-arguments')
                        .remove('no-permissions')
                )
                .migrate(3.0, m -> m.rename('greeting', 'general.greeting')
                        .update('error.not-enough-arguments')
                )

        when:
        def provider = MessageProvider.resource(workDir, resource, log, version)

        then:
        provider.messages == [
                'prefix'                    : 'Blocksmith',
                'general.greeting'          : '%prefix%Hello, %name%!',
                'error.not-enough-arguments': '%prefix%You did not specify enough arguments'
        ]

        where:
        messages << [
                [
                        'version'             : '1.0',
                        'prefix'              : 'Blocksmith',
                        'not-enough-arguments': '',
                        'no-permissions'      : ''
                ],
                [
                        'version' : 2.0,
                        'prefix'  : 'Blocksmith',
                        'greeting': '%prefix%Hello, %name%!',
                        'error'   : [
                                'not-enough-arguments': 'No arguments!'
                        ]
                ],
                [
                        'prefix' : 'Blocksmith',
                        'general': [
                                'greeting': '%prefix%Hello, %name%!'
                        ],
                        'error'  : [
                                'not-enough-arguments': '%prefix%You did not specify enough arguments'
                        ]
                ],
                [
                        'version': 'invalid',
                        'prefix' : 'Blocksmith',
                        'general': [
                                'greeting': '%prefix%Hello, %name%!'
                        ],
                        'error'  : [
                                'not-enough-arguments': '%prefix%You did not specify enough arguments'
                        ]
                ],
                [
                        'version': 3.0,
                        'prefix' : 'Blocksmith',
                        'general': [
                                'greeting': '%prefix%Hello, %name%!'
                        ],
                        'error'  : [
                                'not-enough-arguments': '%prefix%You did not specify enough arguments'
                        ]
                ]
        ]
    }

}