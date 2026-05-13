package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.testcontainers.containers.JdbcDatabaseContainer

import javax.sql.DataSource

abstract class RemoteSqlRepositoryTest extends SqlRepositoryTest {
    protected JdbcDatabaseContainer container

    protected abstract JdbcDatabaseContainer newContainer()

    @Override
    protected DataSource newDataSource() {
        container = newContainer()
        container.start()

        def config = new HikariConfig()
        config.jdbcUrl = container.jdbcUrl
        config.username = 'root'
        config.password = 'test'

        return new HikariDataSource(config)
    }

}
