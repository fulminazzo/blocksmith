package it.fulminazzo.blocksmith.config.jackson

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationAdapter
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.broker.config.MessageBrokerConfig
import spock.lang.Specification

@Slf4j
class MessageBrokerConfigDeserializerIntegrationTest extends Specification {

    def 'test load MessageBrokerConfig from file'() {
        given:
        def configFile = new File('build/resources/integrationTest/broker.yml')

        and:
        def adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML)

        when:
        def config = adapter.load(configFile, MessageBrokerConfig)

        then:
        config != null
    }

    def 'test load MessageBrokerConfig from file throws if type is not recognized'() {
        given:
        def configFile = new File('build/resources/integrationTest/unknown-broker.yml')

        and:
        def adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML)

        when:
        adapter.load(configFile, MessageBrokerConfig)

        then:
        thrown(IOException)
    }

    def 'test load MessageBrokerConfig from file throws if type is not specified'() {
        given:
        def configFile = new File('build/resources/integrationTest/unspecified-broker.yml')

        and:
        def adapter = ConfigurationAdapter.newAdapter(log, ConfigurationFormat.YAML)

        when:
        adapter.load(configFile, MessageBrokerConfig)

        then:
        thrown(IOException)
    }

}
