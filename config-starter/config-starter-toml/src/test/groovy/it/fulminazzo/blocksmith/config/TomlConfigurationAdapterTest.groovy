package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class TomlConfigurationAdapterTest extends MigrationConfigurationAdapterTest {

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
                'comments_enabled = true',
                '# This comment should be',
                '# Multiline!',
                'name = "blocksmith"',
                'description = """',
                'This is the description for the configuration file.',
                'Should be written in multiline format."""',
                'authors = [',
                '    "Fulminazzo",',
                '    "Camilla"',
                ']',
                '',
                '[internal]',
                '# This comment should be indented',
                '# And should be multiline too',
                'version = 1.0'
        ]
    }

    def 'test that store with #array returns #expected'() {
        given:
        def file = getFile('store_array')
        if (file.exists()) file.delete()

        and:
        def adapter = getAdapter()

        when:
        adapter.store(file, ['data': array])

        then:
        noExceptionThrown()

        and:
        file.readLines() == expected

        when:
        def data = adapter.load(file, Map)

        then:
        data['data'] == array

        where:
        array || expected
        []    || ['data = []']
        [1]   || ['data = [', '    1', ']']
    }

    File getFile(final String name) {
        return new File("build/resources/test/${name}.toml")
    }

    BaseConfigurationAdapter getAdapter() {
        return new TomlConfigurationAdapter(log)
    }

}