package it.fulminazzo.blocksmith.config.jackson

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import spock.lang.Specification

@Slf4j
class DataSourceConfigDeserializerTest extends Specification {

    def 'test load DataSourceConfig from file'() {
        given:
        def configFile = new File('build/resources/test/database.yml')

        and:
        def adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML)

        when:
        def config = adapter.load(configFile, DataSourceConfig)

        then:
        config != null
    }

}
