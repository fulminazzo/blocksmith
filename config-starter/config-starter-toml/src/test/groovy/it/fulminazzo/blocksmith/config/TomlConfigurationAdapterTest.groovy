package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j

@Slf4j
class TomlConfigurationAdapterTest extends ConfigurationAdapterTest {

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

    @Override
    protected boolean supportsNull() {
        return false
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/test/${name}.toml")
    }

    @Override
    protected  BaseConfigurationAdapter getAdapter() {
        return new TomlConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
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

}