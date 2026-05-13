package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect

import javax.sql.DataSource

class H2SqlRepositoryTest extends SqlRepositoryTest {
    private static final String H2_PATH = 'jdbc:h2:mem:testdb'

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

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
