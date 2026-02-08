package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

@Slf4j
class ConfigurationFormatTest extends Specification {
    static final @NotNull ConfigurationFormat FORMAT_UNDER_TEST = ConfigurationFormat.YAML

    def 'test that newAdapter does not throw for format under test'() {
        when:
        FORMAT_UNDER_TEST.newAdapter(log)

        then:
        noExceptionThrown()
    }

    def 'test that newAdapter throws for format #configurationFormat'() {
        when:
        configurationFormat.newAdapter(log)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${ConfigurationAdapter.simpleName} for ${configurationFormat.name().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:config-starter-${configurationFormat.name().toLowerCase()} " +
                "is correctly installed."

        where:
        configurationFormat << ConfigurationFormat.values().findAll { it != FORMAT_UNDER_TEST }
    }

}
