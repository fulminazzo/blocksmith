package it.fulminazzo.blocksmith.data.sql

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MySQLContainer

class MySQLRepositoryTest extends RemoteSqlRepositoryTest {

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
        return new MySQLContainer('mysql:8.0.36')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.MYSQL
    }

}
