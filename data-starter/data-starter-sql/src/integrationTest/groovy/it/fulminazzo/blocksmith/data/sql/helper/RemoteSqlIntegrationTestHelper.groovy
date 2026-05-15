package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer

import javax.sql.DataSource
import java.util.concurrent.ConcurrentHashMap

abstract class RemoteSqlIntegrationTestHelper extends SqlIntegrationTestHelper {
    private static final Map<SQLDialect, JdbcDatabaseContainer> CONTAINERS = new ConcurrentHashMap<>()

    protected abstract JdbcDatabaseContainer newContainer()

    @Override
    protected DataSource newDataSource() {
        def container = CONTAINERS.computeIfAbsent(
                dialect,
                d -> {
                    def c = newContainer()
                    c.start()
                    return c
                }
        )

        def config = new HikariConfig()
        config.jdbcUrl = container.jdbcUrl
        config.username = 'root'
        config.password = 'test'

        return new HikariDataSource(config)
    }

}
