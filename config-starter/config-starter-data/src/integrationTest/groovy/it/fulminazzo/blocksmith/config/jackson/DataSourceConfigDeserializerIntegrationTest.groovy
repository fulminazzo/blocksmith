package it.fulminazzo.blocksmith.config.jackson

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import spock.lang.Specification

@Slf4j
class DataSourceConfigDeserializerIntegrationTest extends Specification {

    def 'test load DataSourceConfig from file'() {
        given:
        def configFile = new File('build/resources/integrationTest/database.yml')

        and:
        def adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML)

        when:
        def config = adapter.load(configFile, DataSourceConfig)

        then:
        config != null
    }

}
