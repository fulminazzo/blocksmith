package it.fulminazzo.blocksmith.data.sql.helper

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer

final class MariaDBIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    SQLDialect getDialect() {
        return SQLDialect.MARIADB
    }

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new MariaDBContainer('mariadb:11.4.10')
    }

}
