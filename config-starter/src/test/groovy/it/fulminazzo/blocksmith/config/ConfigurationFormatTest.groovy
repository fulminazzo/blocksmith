package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ConfigurationFormatTest extends Specification {

    def 'test that newAdapter throws for format #configurationFormat'() {
        when:
        configurationFormat.newAdapter(log)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${ConfigurationAdapter.simpleName} for ${configurationFormat.name().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:config-starter-${configurationFormat.name().toLowerCase()} " +
                "is correctly installed."

        where:
        configurationFormat << ConfigurationFormat.values()
    }

}
