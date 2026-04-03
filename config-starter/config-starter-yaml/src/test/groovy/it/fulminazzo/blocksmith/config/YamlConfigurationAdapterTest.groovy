package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j

@Slf4j
class YamlConfigurationAdapterTest extends ConfigurationAdapterTest {

    @Override
    protected boolean supportsNull() {
        return true
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/test/${name}.yml")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new YamlConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
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

}
