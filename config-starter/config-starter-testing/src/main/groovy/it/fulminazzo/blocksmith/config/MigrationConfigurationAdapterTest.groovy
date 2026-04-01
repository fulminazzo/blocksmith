package it.fulminazzo.blocksmith.config

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import spock.lang.Specification

import java.nio.file.Files

abstract class MigrationConfigurationAdapterTest extends Specification {

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
                        'version' : 1.0,
                        'server'  : ['host': '0.0.0.0', 'port': 8080],
                        'database': [
                                'host'    : 'localhost',
                                'port'    : 5432,
                                'user'    : 'admin',
                                'password': 'password123'
                        ]
                ],
                [
                        'version' : 2.0,
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

    protected abstract File getFile(final String name)

    protected abstract BaseConfigurationAdapter getAdapter()

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
