package it.fulminazzo.blocksmith.data.sql.helper

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MySQLContainer

final class MySQLIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new MySQLContainer('mysql:8.0.36')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MYSQL
    }

}
