package it.fulminazzo.blocksmith.config

import groovy.util.logging.Slf4j
import spock.lang.Specification

@Slf4j
class DelegateConfigurationAdapterTest extends Specification {

    def 'test that #method with #arguments delegates'() {
        given:
        def delegate = Mock(BaseConfigurationAdapter)

        and:
        def adapter = new DelegateConfigurationAdapter(log)
        adapter.format = Mock(ConfigurationFormat)
        adapter.delegate = delegate

        when:
        adapter."$method"(*arguments)

        then:
        1 * delegate."$method"(*_) >> { a ->
            assert a == arguments
        }

        where:
        method         || arguments
        'loadComments' || [new ByteArrayInputStream(''.bytes)]
        'load'         || ['', MockConfig]
        'load'         || [new File('build/resources/test/load.json'), MockConfig]
        'load'         || [new ByteArrayInputStream(''.bytes), MockConfig]
        'serialize'    || [new MockConfig()]
        'store'        || [new ByteArrayOutputStream(), new MockConfig()]
        'store'        || [new File('build/resources/test/store.json'), new MockConfig()]
    }

    def 'test that #method with #arguments delegates to #delegateMethod with #delegateArguments'() {
        given:
        def delegate = Mock(BaseConfigurationAdapter)

        and:
        def adapter = new DelegateConfigurationAdapter(log)
        adapter.format = Mock(ConfigurationFormat) {
            it.getFile(_, _) >> { a ->
                return new File(a[0], a[1])
            }
        }
        adapter.delegate = delegate

        when:
        adapter."$method"(*arguments)

        then:
        1 * delegate."$delegateMethod"(*_) >> { a ->
            assert a == delegateArguments
        }

        where:
        method  | arguments                                                   || delegateMethod | delegateArguments
        'load' || [new File('build/resources/test'), 'load.json', MockConfig] || 'load'         | [new File('build/resources/test/load.json'), MockConfig]
        'store' || [new File('build/resources/test'), 'store.json', MockConfig] || 'store'         | [new File('build/resources/test/store.json'), MockConfig]
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
