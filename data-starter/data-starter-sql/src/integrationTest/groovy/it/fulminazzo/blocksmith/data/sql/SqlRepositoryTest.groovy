package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import org.jetbrains.annotations.NotNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.SQLDataType
import spock.lang.Shared

import javax.sql.DataSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.*

abstract class SqlRepositoryTest extends RepositoryTest<SqlRepository<User, Long, Table<? extends Record>>> {
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private final ExecutorService executor = Executors.newSingleThreadExecutor()

    @Shared
    private DataSource dataSource

    @Shared
    private DSLContext dsl

    void setupSuite() {
        dataSource = newDataSource()

        dsl = using(dataSource, dialect)
        dsl.createTableIfNotExists(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN))
                .execute()
    }

    void cleanupSuite() {
        dataSource?.close()
    }

    void setupSingle() {
        setupRepository()
    }

    void cleanupSingle() {
        executor?.shutdown()
        clearData()
    }

    @Override
    SqlRepository<User, Long, Table<? extends Record>> initializeRepository() {
        def table = dsl.meta().getTables(TABLE_NAME).last
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
        return dsl.selectFrom(repository.queryEngine.table)
                .where(repository.queryEngine.idEquals(id))
                .fetch()
                .notEmpty
    }

    @Override
    void insert(final @NotNull User entity) {
        dsl.insertInto(repository.queryEngine.table)
                .values(entity.id, entity.username, entity.age)
                .execute()
    }

    @Override
    void remove(final @NotNull Long id) {
        dsl.deleteFrom(repository.queryEngine.table)
                .where(repository.queryEngine.idEquals(id))
                .execute()
    }

    protected abstract DataSource newDataSource()

    protected abstract SQLDialect getDialect()

}
