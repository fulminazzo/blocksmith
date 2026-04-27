package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class GeneralConfigurationAdapterTest extends Specification {

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
