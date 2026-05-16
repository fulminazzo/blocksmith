package it.fulminazzo.blocksmith.data.cache.helper

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.fulminazzo.blocksmith.data.*
import it.fulminazzo.blocksmith.data.redis.RedisDataSource
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.SqlDataSource
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings
import org.jooq.*
import org.jooq.impl.SQLDataType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgreSQLContainer

import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint
import static org.jooq.impl.DSL.using

final class CachedIntegrationTestHelper implements Closeable {
    private static final int CACHE_PORT = 6379
    private static final int BASE_PORT = DatabaseType.POSTGRESQL.port
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private static final GenericContainer CACHE_SERVER = new GenericContainer('redis:7-alpine').withExposedPorts(CACHE_PORT)
    private static final JdbcDatabaseContainer BASE_SERVER = new PostgreSQLContainer('postgres:18.3')
            .withUsername('root')
            .withPassword('test')

    final ExecutorService executor = Executors.newCachedThreadPool()

    final RepositoryDataSource<? extends RepositorySettings> cacheDataSource

    final RepositoryDataSource<? extends RepositorySettings> baseDataSource

    private final DSLContext context

    CachedIntegrationTestHelper() {
        cacheDataSource = RedisDataSource.builder()
                .uri { it.withHost(cacheServerHost).withPort(cacheServerPort) }
                .build()

        baseDataSource = SqlDataSource.builder()
                .executor(executor)
                .databaseType(DatabaseType.POSTGRESQL)
                .host(baseServerHost)
                .port(baseServerPort)
                .database('test')
                .username('root')
                .password('test')
                .postgres()
                .build()

        context = using(baseDataSource.dataSource, SQLDialect.POSTGRES)
        context.createTableIfNotExists(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN))
                .execute()
    }

    CacheRepository<User, Long> getCacheRepository() {
        return cacheDataSource.newRepository(User, cacheSettings)
    }

    CacheRepositorySettings getCacheSettings() {
        return new RedisRepositorySettings()
                .withDatabaseName('test')
                .withCollectionName('users')
                .withTtl(Duration.ofMinutes(1L))
    }

    Repository<User, Long> getBaseRepository() {
        return baseDataSource.newRepository(User, baseSettings)
    }

    RepositorySettings getBaseSettings() {
        return new SqlRepositorySettings()
                .withTable(table)
                .withIdColumn(column)
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    Table<? extends Record> getTable() {
        return context.meta().getTables(TABLE_NAME).last
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    TableField<? extends Record, Long> getColumn() {
        return table.field(ID_COLUMN) as TableField<? extends Record, Long>
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    @Override
    void close() throws IOException {
        cacheDataSource?.close()
        baseDataSource?.close()
        executor?.close()
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static String getCacheServerHost() {
        return cacheContainer.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static int getCacheServerPort() {
        return cacheContainer.getMappedPort(CACHE_PORT)
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static String getBaseServerHost() {
        return baseContainer.host
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods') // enforce our ordering
    static int getBaseServerPort() {
        return baseContainer.getMappedPort(BASE_PORT)
    }

    @SuppressFBWarnings('MS_EXPOSE_REP')
    static GenericContainer getCacheContainer() {
        if (!CACHE_SERVER.created) CACHE_SERVER.start()
        return CACHE_SERVER
    }

    @SuppressFBWarnings('MS_EXPOSE_REP')
    static GenericContainer getBaseContainer() {
        if (!BASE_SERVER.created) BASE_SERVER.start()
        return BASE_SERVER
    }

}
