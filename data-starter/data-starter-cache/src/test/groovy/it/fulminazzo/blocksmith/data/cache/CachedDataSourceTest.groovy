package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.mapper.MapperFormat
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.redis.RedisDataSource
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.sql.SqlDataSource
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import redis.embedded.RedisServer
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint

class CachedDataSourceTest extends Specification {
    private static final int serverPort = 16479

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static RedisServer server

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()
    }

    void cleanupSpec() {
        server?.stop()
        executor?.shutdown()
    }

    def 'test that server is online'() {
        expect:
        server.active
    }

    def 'test datasource life cycle'() {
        given:
        def dataSource = CachedDataSource.create(
                RedisDataSource.builder()
                        .uri(b -> b.withHost('localhost').withPort(serverPort))
                        .clientOptions(c -> c.autoReconnect(false))
                        .socketOptions(s -> s.keepAlive(true))
                        .mapper(MapperFormat.JSON.newMapper())
                        .build(),
                SqlDataSource.builder()
                        .executor(executor)
                        .database('test')
                        .username('sa')
                        .password('')
                        .h2()
                        .memory()
                        .build()
        )

        and:
        def dsl = DSL.using(dataSource.repositoryDataSource.dataSource, SQLDialect.H2)
        dsl.createTable('TEST')
                .column('ID', SQLDataType.BIGINT.notNull().identity(true))
                .constraints(constraint('PK_TEST').primaryKey('ID'))
                .execute()
        def table = dsl.meta().getTables('TEST')[0]
        def field = table.field('ID')

        when:
        def repository = dataSource.newRepository(
                User,
                CachedRepositorySettings.combine(
                        new RedisRepositorySettings()
                                .withDatabaseName("database")
                                .withCollectionName("users")
                                .withTtl(Duration.ofMinutes(30)),
                        new SqlRepositorySettings()
                                .withTable(table)
                                .withIdColumn(field as TableField<? extends Record, ?>)
                )
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    def 'test hybrid datasource life cycle'() {
        given:
        def dataSource = CachedDataSource.hybrid(
                MemoryDataSource.create(executor),
                RedisDataSource.builder()
                        .uri(b -> b.withHost('localhost').withPort(serverPort))
                        .clientOptions(c -> c.autoReconnect(false))
                        .socketOptions(s -> s.keepAlive(true))
                        .mapper(MapperFormat.JSON.newMapper())
                        .build(),
                SqlDataSource.builder()
                        .executor(executor)
                        .database('test')
                        .username('sa')
                        .password('')
                        .h2()
                        .memory()
                        .build()
        )

        and:
        def internalCacheField = CachedDataSource.getDeclaredField('repositoryDataSource')
        internalCacheField.accessible = true
        def internalCache = internalCacheField.get(dataSource)

        def dsl = DSL.using(internalCache.repositoryDataSource.dataSource, SQLDialect.H2)
        dsl.createTable('TEST')
                .column('ID', SQLDataType.BIGINT.notNull().identity(true))
                .constraints(constraint('PK_TEST').primaryKey('ID'))
                .execute()
        def table = dsl.meta().getTables('TEST')[0]
        def field = table.field('ID')

        when:
        def repository = dataSource.newRepository(
                User,
                CachedRepositorySettings.combine(
                        new MemoryRepositorySettings().withTtl(Duration.ofMinutes(5)),
                        CachedRepositorySettings.combine(
                                new RedisRepositorySettings()
                                        .withDatabaseName("database")
                                        .withCollectionName("users")
                                        .withTtl(Duration.ofMinutes(30)),
                                new SqlRepositorySettings()
                                        .withTable(table)
                                        .withIdColumn(field as TableField<? extends Record, ?>)
                        )
                )
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

}
