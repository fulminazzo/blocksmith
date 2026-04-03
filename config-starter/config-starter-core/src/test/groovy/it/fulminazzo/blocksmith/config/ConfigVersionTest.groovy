package it.fulminazzo.blocksmith.config

import spock.lang.Specification

class ConfigVersionTest extends Specification {

    def 'test that migrate of #version and #data returns updated data'() {
        given:
        def expected = [
                'valid'        : true,
                'database.host': '127.0.0.1',
                'database.port': 3306,
                'database.type': 'SQL',
                'last-update'  : '01-04-2026'
        ]

        and:
        def configVersion = ConfigVersion.of(3.0)
                .migrate(1.0, m -> m.add('host', '127.0.0.1')
                        .add('port', 3306)
                        .add('version', 1.0)
                )
                .migrate(2.0, m -> m.rename('host', 'database.host')
                        .rename('port', 'database.port')
                        .remove('version')
                        .add('last-update', '31-03-2026')
                )
                .migrate(3.0, m -> m.add('valid', true)
                        .add('database.type', 'SQL')
                        .update('last-update', '01-04-2026')
                )

        when:
        def actual = configVersion.applyMigrations(version, data)

        then:
        actual == expected

        where:
        version | data
        0.0     | [:]
        1.0     | ['host': '127.0.0.1', 'port': 3306, 'version': 1.0]
        2.0     | [
                'database.host': '127.0.0.1',
                'database.port': 3306,
                'last-update'  : '31-03-2026'
        ]
        3.0     | [
                'valid'        : true,
                'database.host': '127.0.0.1',
                'database.port': 3306,
                'database.type': 'SQL',
                'last-update'  : '01-04-2026'
        ]
    }

    def 'test that migrate of existing migration throws'() {
        given:
        def version = new ConfigVersion(1.0).migrate(2.0, m -> m)

        when:
        version.migrate(2.0, m -> m)

        then:
        thrown(IllegalArgumentException)
    }

}
