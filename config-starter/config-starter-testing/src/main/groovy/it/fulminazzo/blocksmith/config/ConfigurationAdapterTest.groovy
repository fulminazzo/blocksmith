package it.fulminazzo.blocksmith.config

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import spock.lang.Specification

import java.nio.file.Files

abstract class ConfigurationAdapterTest extends Specification {

    def 'test that load correctly loads raw data'() {
        given:
        def data = getFile('load').readLines().join('\n')

        when:
        def actual = adapter.load(data, MockConfig)

        then:
        noExceptionThrown()

        and:
        actual == expectedConfig
    }

    def 'test that load correctly loads file'() {
        given:
        def file = getFile('load')

        when:
        def actual = adapter.load(file, MockConfig)

        then:
        noExceptionThrown()

        and:
        actual == expectedConfig
    }

    def 'test that load correctly loads stream'() {
        given:
        def stream = new FileInputStream(getFile('load'))

        when:
        def actual = adapter.load(stream, MockConfig)

        then:
        noExceptionThrown()

        and:
        actual == expectedConfig

        and:
        !stream.channel.open
    }

    def 'test that serialize correctly writes data'() {
        when:
        def actual = adapter.serialize(new MockConfig())

        then:
        noExceptionThrown()

        and:
        actual.trim() == expectedStoreLines.join('\n').trim()
    }

    def 'test that store correctly saves file'() {
        given:
        def file = getFile('store')
        if (file.exists()) file.delete()

        when:
        adapter.store(file, new MockConfig())

        then:
        noExceptionThrown()

        and:
        file.readLines() == expectedStoreLines
    }
    
    def 'test that store correctly writes to stream'() {
        given:
        def file = getFile('store')
        if (file.exists()) file.delete()
        file.createNewFile()
        
        and:
        def output = new FileOutputStream(file)

        when:
        adapter.store(output, new MockConfig())

        then:
        noExceptionThrown()

        and:
        file.readLines() == expectedStoreLines
        
        and:
        !output.channel.open
    }

    def 'test that configuration with #data is correctly migrated'() {
        given:
        def adapter = getAdapter()

        and:
        if (data instanceof Map)
            data = renameMap(data, adapter.delegate.mapper.propertyNamingStrategy)

        and:
        def file = getFile('migration/config')
        Files.deleteIfExists(file.toPath())
        adapter.store(file, data)

        when:
        def loaded = adapter.load(file, MigrationConfig)

        then:
        noExceptionThrown()

        and:
        loaded == new MigrationConfig()

        where:
        data << [
                [
                        'version' : 1.0d,
                        'server'  : ['host': '0.0.0.0', 'port': 8080],
                        'database': [
                                'host'    : 'localhost',
                                'port'    : 5432,
                                'user'    : 'admin',
                                'password': 'password123'
                        ]
                ],
                [
                        'version' : 2.0d,
                        'server'  : ['host': '0.0.0.0', 'port': 8080, 'timeoutSeconds': 30],
                        'database': [
                                'host'          : 'localhost',
                                'port'          : 5432,
                                'user'          : 'admin',
                                'password'      : 'password123',
                                'maxConnections': 100
                        ],
                        'features': ['enableBetaUi': true, 'enableMetrics': false]
                ],
                new MigrationConfig()
        ]
    }

    protected MockConfig getExpectedConfig() {
        return new MockConfig(
                false,
                'Blocksmith',
                supportsNull() ? null : '',
                ['Fulminazzo', 'Camilla', 'Alex'],
                new MockConfig.Internal(1.0, isProperties() ? false : null)
        )
    }

    /**
     * Special case for properties tests that do not support <code>null</code>
     * under any circumstance.
     *
     * @return <code>true</code> if the test is for the properties configuration adapter
     */
    protected boolean isProperties() {
        return false
    }

    protected abstract boolean supportsNull()

    protected abstract File getFile(final String name)

    protected abstract BaseConfigurationAdapter getAdapter()

    protected abstract List<String> getExpectedStoreLines()

    private static Map<String, Object> renameMap(final Map<String, Object> map, final PropertyNamingStrategy strategy) {
        if (strategy == null) return map
        for (def key in [*map.keySet()]) {
            def value = map.remove(key)
            if (value instanceof Map) value = renameMap(value, strategy)
            map.put(strategy.nameForField(null, null, key), value)
        }
        return map
    }

}
