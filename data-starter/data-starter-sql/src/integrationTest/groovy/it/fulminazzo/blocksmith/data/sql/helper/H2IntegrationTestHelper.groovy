package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect

import javax.sql.DataSource

final class H2IntegrationTestHelper extends SqlIntegrationTestHelper {
    private static final String H2_PATH = 'jdbc:h2:file:./build/resources/integrationTest/h2;DATABASE_TO_LOWER=TRUE'

    @Override
    SQLDialect getDialect() {
        return SQLDialect.H2
    }

    @Override
    protected DataSource newDataSource() {
        def config = new HikariConfig()
        config.jdbcUrl = H2_PATH
        config.username = 'root'
        config.password = 'test'

        return new HikariDataSource(config)
    }

}
