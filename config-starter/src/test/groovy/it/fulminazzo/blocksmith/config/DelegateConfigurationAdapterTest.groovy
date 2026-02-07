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
        def file = new File('build/resources/test/delegate.test')

        when:
        adapter.store(file, data)

        then:
        noExceptionThrown()

        when:
        def actual = adapter.load(file, data.class)

        then:
        actual == data

        where:
        format << ConfigurationFormat.values()
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
