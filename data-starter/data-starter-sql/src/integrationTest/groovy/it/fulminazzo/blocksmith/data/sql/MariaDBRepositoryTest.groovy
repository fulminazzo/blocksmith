package it.fulminazzo.blocksmith.data.sql

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer

class MariaDBRepositoryTest extends RemoteSqlRepositoryTest {

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
    protected JdbcDatabaseContainer newContainer() {
        return new MariaDBContainer('mariadb:11.4.10')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MARIADB
    }

}
