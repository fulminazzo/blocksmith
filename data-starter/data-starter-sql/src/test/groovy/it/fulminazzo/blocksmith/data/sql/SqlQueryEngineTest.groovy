package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.Users
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint
import static org.jooq.impl.DSL.using

class SqlQueryEngineTest extends Specification {
    private static final String H2_PATH = 'jdbc:h2:mem:testdb'
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static HikariDataSource dataSource
    private static DSLContext dsl

    private static Table<?> table

    private static SqlQueryEngine<?, ?, ?> queryEngine

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

        table = dsl.meta().getTables(TABLE_NAME)[1]

        queryEngine = new SqlQueryEngine<>(
                dsl,
                table,
                table.field(ID_COLUMN),
                executor
        )
    }

    void cleanupSpec() {
        executor?.shutdown()
        dataSource?.close()
    }

    def 'test that batched executes multiple operations at once'() {
        given:
        def users = [Users.SAVED1, Users.SAVED2, Users.NEW1, Users.NEW2]

        when:
        queryEngine.batched((dsl, table) -> {
            for (def user : users) {
                def record = dsl.newRecord(queryEngine.wildcardTable, user)
                dsl.insertInto(queryEngine.wildcardTable).set(record).execute()
            }
        }).get()

        then:
        noExceptionThrown()

        when:
        def found = queryEngine.query((dsl, table) -> {
            def results = []
            for (def user : users)
                results.add(dsl.selectFrom(table)
                        .where(queryEngine.idEquals(user.id))
                        .fetchOneInto(User))
            return results
        }).get()

        then:
        found != null
        found.sort() == users.sort()

        cleanup:
        users.each {
            dsl.deleteFrom(table).where(DSL.field(table.field(ID_COLUMN)).eq(it.id)).execute()
        }
    }

    def 'test that query works'() {
        given:
        def user = Users.SAVED1

        when:
        queryEngine.query((dsl, table) -> {
            def record = dsl.newRecord(queryEngine.wildcardTable, user)
            dsl.insertInto(queryEngine.wildcardTable).set(record).execute()
        }).get()

        then:
        noExceptionThrown()

        when:
        def found = queryEngine.query((dsl, table) -> {
            return dsl.selectFrom(table)
                    .where(queryEngine.idEquals(user.id))
                    .fetchOneInto(User)
        }).get()

        then:
        found != null
        found == user

        cleanup:
        dsl.deleteFrom(table).where(DSL.field(table.field(ID_COLUMN)).eq(user.id)).execute()
    }

}
