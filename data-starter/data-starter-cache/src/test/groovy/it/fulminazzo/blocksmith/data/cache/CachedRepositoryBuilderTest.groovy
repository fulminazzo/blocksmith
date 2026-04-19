package it.fulminazzo.blocksmith.data.cache

import it.fulminazzo.blocksmith.data.User
import it.fulminazzo.blocksmith.data.entity.EntityMapper
import it.fulminazzo.blocksmith.data.mapper.Mappers
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.redis.RedisDataSource
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.sql.SqlDataSource
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import redis.embedded.RedisServer
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint

class CachedRepositoryBuilderTest extends Specification {
    private static final int serverPort = 16478

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static RedisServer server

    private static SqlDataSource sqlDataSource
    private static RedisDataSource redisDataSource
    private static MemoryDataSource memoryDataSource

    private static Table<? extends Record> table
    private static TableField<? extends Record, ?> idColumn

    void setupSpec() {
        server = new RedisServer(serverPort)
        server.start()

        sqlDataSource = SqlDataSource.builder()
                .executor(executor)
                .database('test')
                .username('sa')
                .password('')
                .h2()
                .memory()
                .build()

        redisDataSource = RedisDataSource.builder()
                .uri(b -> b.withHost('localhost').withPort(serverPort))
                .clientOptions(c -> c.autoReconnect(false))
                .socketOptions(s -> s.keepAlive(true))
                .mapper(Mappers.JSON)
                .build()

        memoryDataSource = MemoryDataSource.create(executor)

        def dsl = DSL.using(sqlDataSource.dataSource, SQLDialect.H2)
        dsl.createTable('TEST')
                .column('ID', SQLDataType.BIGINT.notNull().identity(true))
                .constraints(constraint('PK_TEST').primaryKey('ID'))
                .execute()
        table = dsl.meta().getTables('TEST')[0]
        idColumn = table.field('ID') as TableField<? extends Record, ?>
    }

    void cleanupSpec() {
        memoryDataSource?.close()
        redisDataSource?.close()
        sqlDataSource?.close()
        server?.stop()
        executor?.shutdown()
    }

    def 'test that server is online'() {
        expect:
        server.active
    }

    def 'test creation of repository'() {
        given:
        def mainRepository = sqlDataSource.newRepository(
                EntityMapper.create(User.class),
                new SqlRepositorySettings()
                        .withTable(table)
                        .withIdColumn(idColumn)
        )

        when:
        def repository = CachedRepository.wrap(mainRepository)
                .entityType(User)
                .cacheRepository(
                        redisDataSource,
                        new RedisRepositorySettings()
                                .withDatabaseName("database")
                                .withCollectionName("users")
                                .withTtl(Duration.ofMinutes(30))
                )
                .build()

        then:
        noExceptionThrown()

        and:
        repository != null
    }

    def 'test creation of hybrid repository'() {
        given:
        def mainRepository = sqlDataSource.newRepository(
                EntityMapper.create(User.class),
                new SqlRepositorySettings()
                        .withTable(table)
                        .withIdColumn(idColumn)
        )

        when:
        def repository = CachedRepository.wrap(mainRepository)
                .entityType(User)
                .cacheRepository(
                        redisDataSource,
                        new RedisRepositorySettings()
                                .withDatabaseName("database")
                                .withCollectionName("users")
                                .withTtl(Duration.ofMinutes(30))
                )
                .hybrid(
                        memoryDataSource,
                        new MemoryRepositorySettings().withTtl(Duration.ofMinutes(5))
                )

        then:
        noExceptionThrown()

        and:
        repository != null
    }

    def 'test creation of hybrid specific repository'() {
        given:
        def mainRepository = sqlDataSource.newRepository(
                EntityMapper.create(User.class),
                new SqlRepositorySettings()
                        .withTable(table)
                        .withIdColumn(idColumn)
        )

        when:
        def repository = CachedRepository.wrap(mainRepository)
                .entityType(User)
                .cacheRepository(
                        redisDataSource,
                        new RedisRepositorySettings()
                                .withDatabaseName("database")
                                .withCollectionName("users")
                                .withTtl(Duration.ofMinutes(30))
                )
                .hybrid(memoryDataSource.newRepository(
                        User,
                        new MemoryRepositorySettings().withTtl(Duration.ofMinutes(5))
                ))

        then:
        noExceptionThrown()

        and:
        repository != null
    }

}
