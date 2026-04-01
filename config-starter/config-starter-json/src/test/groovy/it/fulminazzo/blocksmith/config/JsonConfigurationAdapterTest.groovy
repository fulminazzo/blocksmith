package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class JsonConfigurationAdapterTest extends MigrationConfigurationAdapterTest {

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
                '{',
                '  "commentsEnabled": true,',
                '  "name": "blocksmith",',
                '  "description": "This is the description for the configuration file.\\nShould be written in multiline format.",',
                '  "authors": [',
                '    "Fulminazzo",',
                '    "Camilla"',
                '  ],',
                '  "internal": {',
                '    "version": 1.0,',
                '    "verified": null',
                '  }',
                '}'
        ]
    }

    File getFile(final String name) {
        return new File("build/resources/test/${name}.json")
    }

    BaseConfigurationAdapter getAdapter() {
        return new JsonConfigurationAdapter(log)
    }

}