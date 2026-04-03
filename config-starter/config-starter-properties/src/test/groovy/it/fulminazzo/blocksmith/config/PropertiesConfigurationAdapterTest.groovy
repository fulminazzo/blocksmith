package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j

@Slf4j
class PropertiesConfigurationAdapterTest extends ConfigurationAdapterTest {

    @Override
    protected boolean isProperties() {
        return true
    }

    @Override
    protected boolean supportsNull() {
        return false
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/test/${name}.properties")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new PropertiesConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return  [
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

}
