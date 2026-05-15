package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect

import javax.sql.DataSource

final class H2IntegrationTestHelper extends SqlIntegrationTestHelper {
    private static final String H2_PATH = 'jdbc:h2:mem:testdb'

    @Override
    protected DataSource newDataSource() {
        def config = new HikariConfig()
        config.jdbcUrl = H2_PATH
        config.username = 'sa'
        config.password = ''

        return new HikariDataSource(config)
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.H2
    }

}