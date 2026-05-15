package it.fulminazzo.blocksmith.data.sql.helper

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer

final class MariaDBIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new MariaDBContainer('mariadb:11.4.10')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MARIADB
    }

}
