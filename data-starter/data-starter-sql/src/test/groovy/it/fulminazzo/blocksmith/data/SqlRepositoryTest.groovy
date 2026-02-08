package it.fulminazzo.blocksmith.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.annotations.NotNull
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

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
                .column(ID_COLUMN, SQLDataType.INTEGER.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
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
    Repository<User, Integer> initializeRepository() {
        return new SqlRepository<>(
                dsl,
                TABLE_NAME,
                ID_COLUMN,
                User
        )
    }

    @Override
    boolean exists(final @NotNull Integer id) {
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
