package it.fulminazzo.blocksmith.data.sql.config

import com.zaxxer.hikari.pool.HikariPool
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import org.h2.jdbc.JdbcSQLNonTransientConnectionException
import spock.lang.Specification

class SqlDataSourceFactoryTest extends Specification {

    private final SqlDataSourceFactory factory = new SqlDataSourceFactory()

    def 'test build with all parameters'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .database('test')
                .username('sa')
                .password('')
                .maximumPoolSize(20)
                .minimumIdle(5)
                .connectionTimeout(30 * 1000)
                .idleTimeout(10 * 60 * 1000)
                .maxLifeTime(30 * 60 * 1000)
                .properties(['prepStmtCacheSize': 250])
                .databaseType(DatabaseType.H2)
                .build()

        when:
        def dataSource = factory.build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()
    }

    /*
     * H2
     */

    def 'test buildH2 with #connectionMode'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .database('test')
                .databaseType(DatabaseType.H2)
                .connectionMode(connectionMode)
                .build()

        when:
        def dataSource = factory.build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        connectionMode << [
                SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.MEMORY)
                        .build(),
                SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.DISK)
                        .directoryPath('./build/resources/test/h2')
                        .build()
        ]
    }

    def 'test buildH2 with SERVER connection mode type'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .database('test')
                .databaseType(DatabaseType.H2)
                .connectionMode(SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.SERVER)
                        .host('0.0.0.0')
                        .port(23578)
                        .build())
                .build()

        when:
        factory.build(config)

        then:
        def e = thrown(HikariPool.PoolInitializationException)
        (e.cause instanceof JdbcSQLNonTransientConnectionException)
        (e.cause.cause instanceof ConnectException)
    }

    /*
     * SQLite
     */

    def 'test buildSQLite with #connectionMode'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .database('test')
                .databaseType(DatabaseType.SQLITE)
                .connectionMode(connectionMode)
                .build()

        when:
        def dataSource = factory.build(config)

        then:
        dataSource != null

        cleanup:
        dataSource?.close()

        where:
        connectionMode << [
                SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.MEMORY)
                        .build(),
                SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.DISK)
                        .directoryPath('./build/resources/test/sqlite')
                        .build()
        ]
    }

    def 'test buildSQLite with SERVER connection mode type'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .database('test')
                .databaseType(DatabaseType.SQLITE)
                .connectionMode(SqlDataSourceConfig.ConnectionMode.builder()
                        .type(SqlDataSourceConfig.ConnectionModeType.SERVER)
                        .build())
                .build()

        when:
        factory.build(config)

        then:
        thrown(IllegalArgumentException)
    }

    /*
     * REMOTE
     */

    def 'test buildRemote with #databaseType'() {
        given:
        def config = SqlDataSourceConfig.builder()
                .host('127.0.0.1')
                .database('test')
                .databaseType(databaseType)
                .build()

        when:
        factory.build(config)

        then:
        def e = thrown(HikariPool.PoolInitializationException)
        (e.cause.class != RuntimeException)

        where:
        databaseType << [
                DatabaseType.MYSQL,
                DatabaseType.MARIADB,
                DatabaseType.POSTGRESQL
        ]
    }

}
