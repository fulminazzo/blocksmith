package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import org.slf4j.Logger
import spock.lang.Specification

@Slf4j
class ConfigurationFormatTest extends Specification {

    def 'test that newAdapter on #format throws #expected'() {
        when:
        format.newAdapter(log)

        then:
        def e = thrown(expected.class)
        e.message == expected.message

        and:
        def eCause = expected.cause
        def cause = e.cause

        if (eCause == null) assert cause == null
        else {
            assert cause != null
            assert cause.class == eCause.class
            assert cause.message == eCause.message
        }

        where:
        format                   || expected
        ConfigurationFormat.JSON || new IllegalArgumentException(
                "Could not find constructor ${JsonConfigurationAdapter.canonicalName}(${Logger.canonicalName})"
        )
        ConfigurationFormat.YAML || new RuntimeException('Test runtime exception')
        ConfigurationFormat.TOML || new RuntimeException(new Exception('Test exception'))
    }

}
