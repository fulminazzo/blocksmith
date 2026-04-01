package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class YamlConfigurationAdapterTest extends MigrationConfigurationAdapterTest {

    def 'test that load correctly loads file'() {
        given:
        def file = getFile('load')

        and:
        def adapter = getAdapter()

        when:
        def actual = adapter.load(file, MockConfig)

        then:
        noExceptionThrown()

        and:
        actual == new MockConfig(
                false,
                'Blocksmith',
                null,
                ['Fulminazzo', 'Camilla', 'Alex'],
                new MockConfig.Internal(1.0, null)
        )
    }

    def 'test that store correctly saves file'() {
        given:
        def file = getFile('store')
        if (file.exists()) file.delete()

        and:
        def adapter = getAdapter()

        when:
        adapter.store(file, new MockConfig())

        then:
        noExceptionThrown()

        and:
        file.readLines() == [
                '# Example comment',
                'comments-enabled: true',
                '# This comment should be',
                '# Multiline!',
                'name: \'blocksmith\'',
                'description: |-',
                '  This is the description for the configuration file.',
                '  Should be written in multiline format.',
                'authors:',
                '- \'Fulminazzo\'',
                '- \'Camilla\'',
                'internal:',
                '  # This comment should be indented',
                '  # And should be multiline too',
                '  version: 1.0',
                '  verified: null'
        ]
    }

    File getFile(final String name) {
        return new File("build/resources/test/${name}.yml")
    }

    BaseConfigurationAdapter getAdapter() {
        return new YamlConfigurationAdapter(log)
    }

}
