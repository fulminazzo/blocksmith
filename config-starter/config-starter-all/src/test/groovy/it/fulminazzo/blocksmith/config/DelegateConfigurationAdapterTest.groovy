package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class DelegateConfigurationAdapterTest extends Specification {

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

    def 'test that getFormat throws if not initialized'() {
        given:
        def adapter = new DelegateConfigurationAdapter(log)

        when:
        adapter.format

        then:
        thrown(IllegalStateException)
    }

    def 'test that getDelegate throws if not initialized'() {
        given:
        def adapter = new DelegateConfigurationAdapter(log)

        when:
        adapter.delegate

        then:
        thrown(IllegalStateException)
    }

}
