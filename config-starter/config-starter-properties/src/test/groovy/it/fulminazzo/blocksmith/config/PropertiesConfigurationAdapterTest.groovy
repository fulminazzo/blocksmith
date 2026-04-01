package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class PropertiesConfigurationAdapterTest extends MigrationConfigurationAdapterTest {

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
                '',
                ['Fulminazzo', 'Camilla', 'Alex'],
                new MockConfig.Internal(1.0, false)
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
                'commentsEnabled=true',
                '# This comment should be',
                '# Multiline!',
                'name=blocksmith',
                'description=This is the description for the configuration file.\\nShould be written in multiline format.',
                'authors.1=Fulminazzo',
                'authors.2=Camilla',
                '# This comment should be indented',
                '# And should be multiline too',
                'internal.version=1.0',
                'internal.verified='
        ]
    }

    File getFile(final String name) {
        return new File("build/resources/test/${name}.properties")
    }

    BaseConfigurationAdapter getAdapter() {
        return new PropertiesConfigurationAdapter(log)
    }

}
