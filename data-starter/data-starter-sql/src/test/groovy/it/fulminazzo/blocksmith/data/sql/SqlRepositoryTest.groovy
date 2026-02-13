package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.jetbrains.annotations.NotNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.SQLDataType

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.*

class SqlRepositoryTest extends RepositoryTest<SqlRepository<User, Long, Table<? extends Record>>> {
    private static final String H2_PATH = 'jdbc:h2:mem:testdb'
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static HikariDataSource dataSource
    private static DSLContext dsl

    void setupSpec() {
        def config = new HikariConfig()
        config.jdbcUrl = H2_PATH
        config.username = 'sa'
        config.password = ''

        dataSource = new HikariDataSource(config)

        dsl = using(dataSource, SQLDialect.H2)
        dsl.createTable(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(
                        constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN)
                )
                .execute()
    }

    void setup() {
        setupRepository()
    }

    void cleanup() {
        clearData()
    }

    void cleanupSpec() {
        executor.shutdown()
        dataSource.close()
    }

    @Override
    SqlRepository<User, Long, Table<? extends Record>> initializeRepository() {
        def table = dsl.meta().getTables(TABLE_NAME)[1]
        return new SqlRepository<>(
                new SqlQueryEngine<User, Long, Table<? extends Record>>(
                        dsl,
                        table,
                        table.field(ID_COLUMN, Long),
                        executor
                ),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return dsl.selectFrom(table(TABLE_NAME))
                .where(field(ID_COLUMN).eq(id))
                .fetch()
                .notEmpty
    }

    @Override
    void insert(final @NotNull User entity) {
        dsl.insertInto(table(TABLE_NAME))
                .values(entity.id, entity.username, entity.age)
                .execute()
    }

    @Override
    void remove(final @NotNull Long id) {
        dsl.deleteFrom(table(TABLE_NAME))
                .where(field(ID_COLUMN).eq(id))
                .execute()
    }

}
