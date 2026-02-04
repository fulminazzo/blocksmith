package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class YamlConfigurationAdapterTest extends Specification {

    def 'test that store correctly saves file'() {
        given:
        def file = new File('build/resources/test/store.yml')
        if (file.exists()) file.delete()

        and:
        def adapter = new YamlConfigurationAdapter(log)

        when:
        adapter.store(file, new MockConfig())

        then:
        noExceptionThrown()

        and:
        file.readLines() == [
                '# Example comment',
                'comments-enabled: true',
                '# This comment should be',
                '# Multiline!',
                'name: "blocksmith"',
                'internal:',
                '  # This comment should be indented',
                '  version: 1.0',
                ''
        ]
    }

}
