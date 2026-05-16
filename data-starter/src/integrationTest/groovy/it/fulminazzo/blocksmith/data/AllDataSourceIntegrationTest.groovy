package it.fulminazzo.blocksmith.data

import com.mongodb.AuthenticationMechanism
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.ProjectInfo
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.cache.config.CachedDataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceFactories
import it.fulminazzo.blocksmith.data.file.FileRepositorySettings
import it.fulminazzo.blocksmith.data.file.config.FileDataSourceConfig
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.memory.config.MemoryDataSourceConfig
import it.fulminazzo.blocksmith.data.mongodb.MongoRepositorySettings
import it.fulminazzo.blocksmith.data.mongodb.config.MongoDataSourceConfig
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.redis.config.RedisDataSourceConfig
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings
import it.fulminazzo.blocksmith.data.sql.config.SqlDataSourceConfig
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.Table
import org.jooq.TableField
import org.jooq.SQLDialect
import org.jooq.impl.SQLDataType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Duration

import static org.jooq.impl.DSL.constraint
import static org.jooq.impl.DSL.using

@Slf4j
class AllDataSourceIntegrationTest extends Specification {
    private static final String TABLE_NAME = 'USERS'
    private static final String ID_COLUMN = 'ID'

    private static final int REDIS_PORT = 6379
    private static final int MONGO_PORT = 27017

    private static final JdbcDatabaseContainer SQL_SERVER = new PostgreSQLContainer('postgres:18.3')
            .withUsername('root')
            .withPassword('test')
    private static final GenericContainer REDIS_SERVER = new GenericContainer('redis:7-alpine').withExposedPorts(REDIS_PORT)
    private static final MongoDBContainer MONGO_SERVER = new MongoDBContainer('mongo:7.0')

    @Shared
    private HikariDataSource remoteSqlDataSource

    @Shared
    private DSLContext remoteSqlConnection

    @Shared
    private HikariDataSource h2SqlDataSource

    @Shared
    private DSLContext h2SqlConnection

    @Shared
    private DataSourceConfig memoryDataSourceConfig

    @Shared
    private DataSourceConfig fileDataSourceConfig

    @Shared
    private DataSourceConfig sqlDataSourceConfig

    @Shared
    private DataSourceConfig redisDataSourceConfig

    @Shared
    private DataSourceConfig mongoDataSourceConfig

    void setupSpec() {
        SQL_SERVER.start()
        REDIS_SERVER.start()
        MONGO_SERVER.start()
        MONGO_SERVER.execInContainer(
                'mongosh', '--quiet', '--eval',
                """
        db.getSiblingDB('admin').createUser({
            user: 'root',
            pwd:  'test',
            roles: [{ role: 'root', db: 'admin' }]
        })
        """
        )

        def remoteConfig = new HikariConfig()
        remoteConfig.jdbcUrl = SQL_SERVER.jdbcUrl
        remoteConfig.username = 'root'
        remoteConfig.password = 'test'
        remoteSqlDataSource = new HikariDataSource(remoteConfig)

        remoteSqlConnection = initializeContextAndTable(remoteSqlDataSource, SQLDialect.POSTGRES)

        def h2Config = new HikariConfig()
        h2Config.jdbcUrl = 'jdbc:h2:file:./build/resources/integrationTest/h2;DATABASE_TO_LOWER=TRUE'
        h2Config.username = 'root'
        h2Config.password = 'test'
        h2SqlDataSource = new HikariDataSource(h2Config)

        h2SqlConnection = initializeContextAndTable(h2SqlDataSource, SQLDialect.H2)

        memoryDataSourceConfig = new MemoryDataSourceConfig()
        fileDataSourceConfig = new FileDataSourceConfig()
        sqlDataSourceConfig = new SqlDataSourceConfig()
                .withDatabaseType(DatabaseType.POSTGRESQL)
                .withDatabase('test')
                .withUsername('root')
                .withPassword('test')
                .withMaximumPoolSize(20)
                .withMinimumIdle(5)
                .withConnectionTimeout(30_000)
                .withIdleTimeout(10 * 60_0000)
                .withMaxLifeTime(30 * 60_000)
                .withProperties([
                        'tcpKeepAlive'     : true,
                        'prepareThreshold' : 5
                ])
                .withHost(sqlServerHost)
                .withPort(sqlServerPort)
        redisDataSourceConfig = new RedisDataSourceConfig()
                .withHost(redisServerHost)
                .withPort(redisServerPort)
                .withClientName(ProjectInfo.PROJECT_NAME)
                .withDatabase(0)
                .withSsl(false)
        mongoDataSourceConfig = new MongoDataSourceConfig()
                .withHost(mongoServerHost)
                .withPort(mongoServerPort)
                .withSrvHost(null)
                .withSrvMaxHosts(1)
                .withSrvServiceName(null)
                .withReplicaSetName('rs0')
                .withApplicationName(ProjectInfo.PROJECT_NAME)
                .withCredentials(new MongoDataSourceConfig.MongoCredentialConfig()
                        .withUsername('root')
                        .withPassword('test')
                        .withMechanism(AuthenticationMechanism.SCRAM_SHA_256.name())
                )
    }

    void cleanupSpec() {
        remoteSqlDataSource?.close()

        SQL_SERVER.stop()
        REDIS_SERVER.stop()
        MONGO_SERVER.stop()
    }

    def 'test #dataSourceConfig datasource life cycle'() {
        given:
        final databaseType = (dataSourceConfig instanceof SqlDataSourceConfig)
                ? dataSourceConfig.databaseType
                : null
        final AllRepositorySettings repositorySettings = new AllRepositorySettings(
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofMinutes(1))
                        .withExpirationStrategy(MemoryRepositorySettings.ExpiryStrategy.SCHEDULED),
                new FileRepositorySettings()
                        .withDataDirectory(new File('build/resources/integrationTest'))
                        .withLogger(log)
                        .withFormat(ConfigurationFormat.JSON),
                new SqlRepositorySettings()
                        .withTable(getSqlTable(databaseType))
                        .withIdColumn(getSqlColumn(databaseType)),
                new RedisRepositorySettings()
                        .withDatabaseName('database')
                        .withCollectionName('users')
                        .withTtl(Duration.ofMinutes(1)),
                new MongoRepositorySettings()
                        .withDatabaseName('database')
                        .withCollectionName('users')
        )

        when:
        def dataSource = DataSourceFactories.build(dataSourceConfig)

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(
                User,
                repositorySettings.getRepositorySettings(dataSource)
        )

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()

        where:
        dataSourceConfig << [
                // base
                memoryDataSourceConfig,
                fileDataSourceConfig,
                sqlDataSourceConfig,
                new SqlDataSourceConfig()
                        .withDatabaseType(DatabaseType.H2)
                        .withDatabase('h2')
                        .withUsername('root')
                        .withPassword('test')
                        .withMaximumPoolSize(20)
                        .withMinimumIdle(5)
                        .withConnectionTimeout(30_000)
                        .withIdleTimeout(10 * 60_0000)
                        .withMaxLifeTime(30 * 60_000)
                        .withParameters([
                                'DATABASE_TO_LOWER' : true
                        ])
                        .withSchemaName('PUBLIC')
                        .withConnectionMode(new SqlDataSourceConfig.ConnectionMode()
                                .withType(SqlDataSourceConfig.ConnectionModeType.DISK)
                                .withDirectoryPath('build/resources/integrationTest')
                        ),
                redisDataSourceConfig,
                mongoDataSourceConfig,
                // In-memory cached
                new CachedDataSourceConfig()
                        .withCache(memoryDataSourceConfig)
                        .withRepository(fileDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(memoryDataSourceConfig)
                        .withRepository(sqlDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(memoryDataSourceConfig)
                        .withRepository(redisDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(memoryDataSourceConfig)
                        .withRepository(mongoDataSourceConfig)
                        .withHybrid(false),
                // Redis cached
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(memoryDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(fileDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(sqlDataSourceConfig)
                        .withHybrid(false),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(mongoDataSourceConfig)
                        .withHybrid(false),
                // In-memory, Redis cached
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(memoryDataSourceConfig)
                        .withHybrid(true),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(fileDataSourceConfig)
                        .withHybrid(true),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(sqlDataSourceConfig)
                        .withHybrid(true),
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(mongoDataSourceConfig)
                        .withHybrid(true),
                // Weird wrapping cached
                new CachedDataSourceConfig()
                        .withCache(redisDataSourceConfig)
                        .withRepository(
                                new CachedDataSourceConfig()
                                        .withCache(redisDataSourceConfig)
                                        .withRepository(sqlDataSourceConfig)
                                        .withHybrid(false)
                        )
                        .withHybrid(true)
        ]
    }

    protected Table<? extends Record> getSqlTable(final DatabaseType databaseType) {
        final DSLContext context
        if (databaseType == DatabaseType.H2) context = h2SqlConnection
        else context = remoteSqlConnection
        return context.meta().getTables(TABLE_NAME).last
    }

    protected TableField<? extends Record, Long> getSqlColumn(final DatabaseType databaseType) {
        return getSqlTable(databaseType).field(ID_COLUMN) as TableField<? extends Record, Long>
    }

    protected static String getSqlServerHost() {
        return SQL_SERVER.host
    }

    protected static int getSqlServerPort() {
        return SQL_SERVER.getMappedPort(DatabaseType.POSTGRESQL.port)
    }

    protected static String getRedisServerHost() {
        return REDIS_SERVER.host
    }

    protected static int getRedisServerPort() {
        return REDIS_SERVER.getMappedPort(REDIS_PORT)
    }

    protected static String getMongoServerHost() {
        return MONGO_SERVER.host
    }

    protected static int getMongoServerPort() {
        return MONGO_SERVER.getMappedPort(MONGO_PORT)
    }

    @SuppressWarnings('PublicMethodsBeforeNonPublicMethods')
    // enforce our ordering
    static DSLContext initializeContextAndTable(final DataSource dataSource, final SQLDialect dialect) {
        def context = using(dataSource, dialect)
        context.createTableIfNotExists(TABLE_NAME)
                .column(ID_COLUMN, SQLDataType.BIGINT.notNull().identity(true))
                .column('USERNAME', SQLDataType.VARCHAR(16).notNull())
                .column('AGE', SQLDataType.INTEGER.notNull())
                .constraints(constraint("PK_$TABLE_NAME").primaryKey(ID_COLUMN))
                .execute()
        return context
    }

}
