package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.RepositoryIntegrationTest
import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper
import org.jetbrains.annotations.NotNull
import org.jooq.Record
import org.jooq.Table
import spock.lang.Shared

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

abstract class SqlRepositoryIntegrationTest extends RepositoryIntegrationTest<SqlRepository<User, Long, Table<? extends Record>>> {
    private final ExecutorService executor = Executors.newSingleThreadExecutor()

    @Shared
    private SqlIntegrationTestHelper testHelper

    void setupSuite() {
        testHelper = newTestHelper()
    }

    void cleanupSuite() {
        testHelper?.close()
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
        return new SqlRepository<>(
                new SqlQueryEngine<User, Long, Table<? extends Record>>(
                        testHelper.context,
                        testHelper.table,
                        testHelper.column,
                        executor
                ),
                EntityMapper.create(User)
        )
    }

    @Override
    boolean exists(final @NotNull Long id) {
        return testHelper.context.selectFrom(repository.queryEngine.table)
                .where(repository.queryEngine.idEquals(id))
                .fetch()
                .notEmpty
    }

    @Override
    void insert(final @NotNull User entity) {
        testHelper.context.insertInto(repository.queryEngine.table)
                .values(entity.id, entity.username, entity.age)
                .execute()
    }

    @Override
    void remove(final @NotNull Long id) {
        testHelper.context.deleteFrom(repository.queryEngine.table)
                .where(repository.queryEngine.idEquals(id))
                .execute()
    }

    protected abstract SqlIntegrationTestHelper newTestHelper()

}
