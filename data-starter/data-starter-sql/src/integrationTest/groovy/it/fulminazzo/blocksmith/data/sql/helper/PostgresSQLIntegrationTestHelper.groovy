package it.fulminazzo.blocksmith.data.sql.helper

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer

final class PostgresSQLIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new PostgreSQLContainer('postgres:18.3')
                .withUsername('root')
                .withPassword('test')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.POSTGRES
    }

}
