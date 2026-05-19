package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect

import javax.sql.DataSource

final class SQLiteIntegrationTestHelper extends SqlIntegrationTestHelper {

    @Override
    SQLDialect getDialect() {
        return SQLDialect.SQLITE
    }

    @Override
    protected DataSource newDataSource() {
        def databaseFile = new File('build/resources/integrationTest/sqlite.db')
        databaseFile.parentFile.mkdirs()

        def config = new HikariConfig()
        config.jdbcUrl = "jdbc:sqlite:$databaseFile"

        return new HikariDataSource(config)
    }

}
