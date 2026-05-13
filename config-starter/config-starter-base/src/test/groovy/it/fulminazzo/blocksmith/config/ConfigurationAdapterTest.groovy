package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ConfigurationAdapterTest extends Specification {

    def 'test that newAdapter works'() {
        given:
        def format = Mock(ConfigurationFormat)

        when:
        def adapter = ConfigurationAdapter.newAdapter(log, format)

        then:
        DelegateConfigurationAdapter.isInstance(adapter)
    }

}
