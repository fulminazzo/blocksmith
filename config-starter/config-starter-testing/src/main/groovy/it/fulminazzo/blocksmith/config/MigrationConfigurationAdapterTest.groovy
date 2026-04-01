package it.fulminazzo.blocksmith.config

import spock.lang.Specification

import java.nio.file.Files

abstract class MigrationConfigurationAdapterTest extends Specification {

    def 'test that configuration with #data is correctly migrated'() {
        given:
        def adapter = getAdapter()

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
                        'version': 1.0,
                        'server': ['host': '0.0.0.0', 'port': 8080],
                        'database': [
                                'host': 'localhost',
                                'port': 5432,
                                'user': 'admin',
                                'password': 'password123'
                        ]
                ],
                [
                        'version': 2.0,
                        'server': ['host': '0.0.0.0', 'port': 8080, 'timeoutSeconds': 30],
                        'database': [
                                'host': 'localhost',
                                'port': 5432,
                                'user': 'admin',
                                'password': 'password123',
                                'maxConnections': 100
                        ],
                        'features': ['enableBetaUi': true, 'enableMetrics': false]
                ],
                new MigrationConfig()
        ]
    }

    protected abstract File getFile(final String name)

    protected abstract BaseConfigurationAdapter getAdapter()

}
