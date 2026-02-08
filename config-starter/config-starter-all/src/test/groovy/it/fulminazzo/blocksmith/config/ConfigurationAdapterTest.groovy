package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class ConfigurationAdapterTest extends Specification {

    def 'test documentation functioning'() {
        given:
        def directory = new File('build/resources/test')

        and:
        def yamlFile = new File(directory, 'data.yaml')
        if (yamlFile.exists()) yamlFile.delete()

        and:
        def jsonFile = new File(directory, 'data.json')

        when:
        def logger = log
        def adapter = ConfigurationAdapter.newAdapter(logger, ConfigurationFormat.YAML)
        adapter.store(yamlFile, "Hello, world!")

        adapter.setFormat(ConfigurationFormat.JSON)
        def data = adapter.load(jsonFile, String)

        then:
        yamlFile.exists()

        and:
        yamlFile.readLines() == ['\'Hello, world!\'']

        and:
        data == 'Hello, world!'
    }

}
