package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect

import javax.sql.DataSource

class SQLiteSqlRepositoryTest extends SqlRepositoryTest {

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
        def databaseFile = new File('build/resources/integrationTest/sqlite.db')
        databaseFile.parentFile.mkdirs()

        def config = new HikariConfig()
        config.jdbcUrl = "jdbc:sqlite:$databaseFile"

        return new HikariDataSource(config)
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.SQLITE
    }

}
