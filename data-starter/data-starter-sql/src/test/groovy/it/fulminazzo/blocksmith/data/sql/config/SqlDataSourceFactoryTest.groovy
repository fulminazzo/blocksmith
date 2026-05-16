package it.fulminazzo.blocksmith.data.sql.config

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import spock.lang.Specification

class SqlDataSourceFactoryTest extends Specification {

    def 'test that build from SQLite config of server throws'() {
        given:
        def config = new SqlDataSourceConfig()
                .setDatabaseType(DatabaseType.SQLITE)
                .setConnectionMode(new SqlDataSourceConfig.ConnectionMode()
                        .setType(SqlDataSourceConfig.ConnectionModeType.SERVER)
                )

        when:
        new SqlDataSourceFactory().build(config)

        then:
        thrown(IllegalArgumentException)
    }

}
