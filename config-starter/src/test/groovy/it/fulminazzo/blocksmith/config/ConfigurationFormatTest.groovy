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
        e.message == "Could not find suitable ${ConfigurationAdapter.simpleName} for ${configurationFormat.name().toLowerCase().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:config-starter-${configurationFormat.name().toLowerCase()} " +
                "is correctly installed."

        where:
        configurationFormat << ConfigurationFormat.values()
    }

    def 'test that fromFile of #file returns expected'() {
        when:
        def actual = ConfigurationFormat.fromFile(file)

        then:
        actual == expected

        where:
        file                        || expected
        new File('file.json')       || ConfigurationFormat.JSON
        new File('file.properties') || ConfigurationFormat.PROPERTIES
        new File('file.toml')       || ConfigurationFormat.TOML
        new File('file.xml')        || ConfigurationFormat.XML
        new File('file.yaml')       || ConfigurationFormat.YAML
        new File('file.yml')        || ConfigurationFormat.YAML
    }

    def 'test that fromFile of unknown throws'() {
        given:
        def file = new File('file.txt')

        when:
        ConfigurationFormat.fromFile(file)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Could not find configuration format from file: file.txt'
    }

}
