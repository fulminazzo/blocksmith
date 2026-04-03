package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j

@Slf4j
class JsonConfigurationAdapterTest extends ConfigurationAdapterTest {

    @Override
    protected boolean supportsNull() {
        return true
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/test/${name}.json")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new JsonConfigurationAdapter(log)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
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

}