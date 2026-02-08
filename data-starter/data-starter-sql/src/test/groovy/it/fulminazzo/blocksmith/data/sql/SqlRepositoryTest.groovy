package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.fulminazzo.blocksmith.data.Repository
import it.fulminazzo.blocksmith.data.RepositoryTest
import it.fulminazzo.blocksmith.data.User
import org.jetbrains.annotations.NotNull
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class SqlRepositoryTest extends RepositoryTest {
    private static final String H2_PATH = 'jdbc:h2:mem:testdb'
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private HikariDataSource dataSource
    private DSLContext dsl

    void setup() {
        def config = new HikariConfig()
        config.jdbcUrl = H2_PATH
        config.username = 'sa'
        config.password = ''

        dataSource = new HikariDataSource(config)

        dsl = DSL.using(dataSource, SQLDialect.H2)
        dsl.createTable(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(
                        constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN)
                )
                .execute()

        setupRepository()
    }

    void cleanup() {
        dataSource.close()
    }

    def 'test that query works'() {
        when:
        def actual = repository.query(dsl -> dsl.selectOne().fetchOne(0, Integer)).get()

        then:
        actual == 1
    }

    @Override
    Repository<User, Long> initializeRepository() {
        def table = dsl.meta().getTables(TABLE_NAME)[1]
        return new SqlRepository<>(
                dsl,
                table,
                table.field(ID_COLUMN, Long),
                User,
                Executors.newSingleThreadExecutor()
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
    void insert(final @NotNull User data) {
        dsl.insertInto(table(TABLE_NAME))
                .values(data.id, data.username, data.age)
                .execute()
    }

}
