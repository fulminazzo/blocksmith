package it.fulminazzo.blocksmith.data.sql

import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer

class PostgresSQLRepositoryTest extends RemoteSqlRepositoryTest {

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
        return new PostgreSQLContainer('postgres:18.3')
                .withUsername('root')
                .withPassword('test')
    }

    @Override
    protected SQLDialect getDialect() {
        return SQLDialect.POSTGRES
    }

}
