package it.fulminazzo.blocksmith.config.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.BaseConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationAdapterTest

@Slf4j
class JacksonConfigurationAdapterTest extends ConfigurationAdapterTest {

    @Override
    protected boolean supportsComments() {
        return false
    }

    @Override
    protected boolean supportsNull() {
        return true
    }

    @Override
    protected File getFile(final String name) {
        return new File("build/resources/integrationTest/${name}.json")
    }

    @Override
    protected BaseConfigurationAdapter getAdapter() {
        return new JacksonConfigurationAdapter(new ObjectMapper(), log, null)
    }

    @Override
    protected List<String> getExpectedStoreLines() {
        return [
                '{"commentsEnabled":true,' +
                        '"name":"blocksmith",' +
                        '"description":"This is the description for the configuration file.\\nShould be written in multiline format.",' +
                        '"authors":["Fulminazzo","Camilla"],' +
                        '"internal":{"version":1.0,"verified":null}}'
        ]
    }

}
