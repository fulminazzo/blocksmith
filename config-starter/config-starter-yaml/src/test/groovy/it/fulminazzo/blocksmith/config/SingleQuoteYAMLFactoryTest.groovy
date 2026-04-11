package it.fulminazzo.blocksmith.config

import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder
import org.yaml.snakeyaml.DumperOptions
import spock.lang.Specification

class SingleQuoteYAMLFactoryTest extends Specification {

    def 'test that _createGenerator with #dumperOptions returns instance of SingleQuoteYAMLFactory'() {
        given:
        def factory = new YamlConfigurationAdapter.SingleQuoteYAMLFactory(
                new YAMLFactoryBuilder().dumperOptions(dumperOptions)
        )

        when:
        def generator = factory._createGenerator(Mock(Writer), Mock(IOContext))

        then:
        (generator instanceof YamlConfigurationAdapter.SingleQuoteYAMLGenerator)

        where:
        dumperOptions << [null, new DumperOptions()]
    }

}
