package it.fulminazzo.blocksmith.data.sql.helper

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer

import javax.sql.DataSource
import java.util.concurrent.ConcurrentHashMap

abstract class RemoteSqlIntegrationTestHelper extends SqlIntegrationTestHelper {
    private static final Map<SQLDialect, JdbcDatabaseContainer> CONTAINERS = new ConcurrentHashMap<>()

    protected abstract JdbcDatabaseContainer newContainer()

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    String getServerHost() {
        return container.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    int getServerPort() {
        return container.getMappedPort((
                dialect == SQLDialect.POSTGRES
                        ? DatabaseType.POSTGRESQL
                        : DatabaseType.valueOf(dialect.name.toUpperCase())
        ).port)
    }

    private JdbcDatabaseContainer getContainer() {
        return CONTAINERS.computeIfAbsent(
                dialect,
                d -> {
                    def c = newContainer()
                    c.start()
                    return c
                }
        )
    }

    @Override
    protected DataSource newDataSource() {
        def config = new HikariConfig()
        config.jdbcUrl = container.jdbcUrl
        config.username = 'root'
        config.password = 'test'

        return new HikariDataSource(config)
    }

}
