package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class DelegateConfigurationAdapterIntegrationTest extends Specification {

    def 'test documentation functioning'() {
        given:
        def directory = new File('build/resources/test')
        if (!directory.exists()) directory.mkdirs()

        and:
        def yamlFile = new File(directory, 'data.yaml')
        if (yamlFile.exists()) yamlFile.delete()

        and:
        def jsonFile = new File(directory, 'data.json')
        jsonFile << '"Hello, world!"'

        when:
        def logger = log
        def adapter = ConfigurationAdapter.newAdapter(logger, ConfigurationFormat.YAML)
        adapter.store(yamlFile, 'Hello, world!')

        adapter.format = ConfigurationFormat.JSON
        def data = adapter.load(jsonFile, String)

        then:
        yamlFile.exists()

        and:
        yamlFile.readLines() == ['\'Hello, world!\'']

        and:
        data == 'Hello, world!'
    }

    def 'test that adapter correctly loads and saves #format'() {
        given:
        def adapter = new DelegateConfigurationAdapter(log).setFormat(format)

        and:
        def data = new MockConfig()
        data.internal.verified = true

        and:
        def parentFile = new File('build/resources/test')

        when:
        adapter.store(parentFile, 'delegate', data)

        then:
        noExceptionThrown()

        when:
        def actual = adapter.load(parentFile, 'delegate', data.class)

        then:
        actual == data

        where:
        format << ConfigurationFormat.values()
    }

}
