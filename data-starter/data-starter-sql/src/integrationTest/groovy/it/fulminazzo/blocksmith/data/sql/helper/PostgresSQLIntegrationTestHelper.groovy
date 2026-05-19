package it.fulminazzo.blocksmith.data.sql.helper

import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.IDatabaseType
import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer

final class PostgresSQLIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    SQLDialect getDialect() {
        return SQLDialect.POSTGRES
    }

    @Override
    protected IDatabaseType getDatabaseType() {
        return DatabaseType.POSTGRESQL
    }

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new PostgreSQLContainer('postgres:18.3')
                .withUsername('root')
                .withPassword('test')
    }

}
