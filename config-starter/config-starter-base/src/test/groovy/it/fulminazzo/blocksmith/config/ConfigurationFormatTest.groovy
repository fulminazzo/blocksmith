package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ConfigurationFormatTest extends Specification {

    def 'test that getFile of #format returns #expected'() {
        expect:
        format.getFile(new File('dir'), 'file') == expected

        where:
        format                         || expected
        ConfigurationFormat.JSON       || new File('dir', 'file.json')
        ConfigurationFormat.PROPERTIES || new File('dir', 'file.properties')
        ConfigurationFormat.TOML       || new File('dir', 'file.toml')
        ConfigurationFormat.XML        || new File('dir', 'file.xml')
        ConfigurationFormat.YAML       || new File('dir', 'file.yml')
    }

    def 'test that newAdapter throws for format #configurationFormat'() {
        when:
        configurationFormat.newAdapter(log)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Could not find suitable ${ConfigurationAdapter.simpleName} for ${configurationFormat.name().toLowerCase().capitalize()}. " +
                "Please check that the module it.fulminazzo.blocksmith:config-starter-${configurationFormat.name().toLowerCase()} " +
                'is correctly installed.'

        where:
        configurationFormat << ConfigurationFormat.values()
    }

    def 'test that fromExtension of filename returns #expected'() {
        given:
        def filename = "file.$expected.fileExtension"

        when:
        def format = ConfigurationFormat.fromExtension(filename)

        then:
        format == expected

        where:
        expected << ConfigurationFormat.values()
    }

    def 'test that fromExtension of unknown filename throws'() {
        when:
        ConfigurationFormat.fromExtension('unknown')

        then:
        thrown(IllegalArgumentException)
    }

}
