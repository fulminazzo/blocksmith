package it.fulminazzo.blocksmith.data.sql.helper

import it.fulminazzo.blocksmith.data.sql.IDatabaseType
import org.jooq.SQLDialect
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.YugabyteDBYSQLContainer

final class CustomSqlIntegrationTestHelper extends RemoteSqlIntegrationTestHelper {

    @Override
    IDatabaseType getDatabaseType() {
        return new IDatabaseType() {

            @Override
            String getJdbcName() {
                return 'yugabytedb'
            }

            @Override
            int getPort() {
                return 5433
            }

        }
    }

    @Override
    SQLDialect getDialect() {
        return SQLDialect.YUGABYTEDB
    }

    @Override
    protected JdbcDatabaseContainer newContainer() {
        return new YugabyteDBYSQLContainer('yugabytedb/yugabyte:2.14.4.0-b26')
                .withUsername('root')
                .withPassword('test')
                .withDatabaseName('test')
    }

}
