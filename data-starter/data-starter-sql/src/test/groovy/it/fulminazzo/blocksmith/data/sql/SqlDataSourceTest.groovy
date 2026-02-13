package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import it.fulminazzo.blocksmith.data.User
import org.jetbrains.annotations.NotNull
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import static org.jooq.impl.DSL.constraint

class SqlDataSourceTest extends Specification {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor()

    private static DataSource hikariDataSource
    private static SqlDataSource dataSource
    private static Connection connection

    void setupSpec() {
        def hikariConfig = new HikariConfig()
        hikariConfig.jdbcUrl = 'jdbc:h2:mem:testdb'
        hikariConfig.username = 'sa'
        hikariConfig.password = ''

        hikariDataSource = new HikariDataSource(hikariConfig)
        dataSource = new SqlDataSource(hikariDataSource, SQLDialect.H2, executor)

        connection = hikariDataSource.getConnection()
    }

    void cleanup() {
        DSL.using(hikariDataSource, SQLDialect.H2).dropTableIfExists('LOGINS').execute()
    }

    void cleanupSpec() {
        connection?.close()
        dataSource?.close()
        executor?.shutdown()
    }

    def 'test datasource life cycle'() {
        given:
        def dataSource = SqlDataSource.builder()
                .executor(executor)
                .database('test')
                .username('sa')
                .password('')
                .h2()
                .memory()
                .build()

        and:
        def dsl = DSL.using(dataSource.dataSource, SQLDialect.H2)
        dsl.createTable('TEST')
                .column('ID', SQLDataType.BIGINT.notNull().identity(true))
                .constraints(constraint('PK_TEST').primaryKey('ID'))
                .execute()
        def table = dsl.meta().getTables('TEST')[0]
        def field = table.field('ID')

        when:
        def repository = dataSource.newRepository(
                User,
                table,
                field
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    def 'test executeScriptFromFile of #argument correctly updates database'() {
        when:
        dataSource.executeScriptFromFile(argument)

        then:
        noExceptionThrown()

        when:
        def set = connection.prepareStatement('SELECT * FROM PUBLIC.LOGINS').executeQuery()

        then:
        set.next()

        and:
        set.getString('name') == 'Alex'
        set.getInt('count') == 3

        and:
        !set.next()

        where:
        argument << [
                'build/resources/test/h2_schema.sql',
                new File('build/resources/test/h2_schema.sql')
        ]
    }

    def 'test executeScriptFromResource correctly updates database'() {
        when:
        dataSource.executeScriptFromResource('/h2_schema.sql')

        then:
        noExceptionThrown()

        when:
        def set = connection.prepareStatement('SELECT * FROM PUBLIC.LOGINS').executeQuery()

        then:
        set.next()

        and:
        set.getString('name') == 'Alex'
        set.getInt('count') == 3

        and:
        !set.next()
    }

    /*
     * BUILDER TESTS
     */

    /*
     * REMOTE
     */

    def 'test initialize #type connection'() {
        given:
        def methodName = (type == DatabaseType.MARIADB ?
                DatabaseType.MYSQL : type)
                .name().toLowerCase()

        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('sql_data_source')
                .username('user')
                .password('password')
                .databaseType(type)
                .host('localhost')
                .port(23692)
                ."${methodName}"()
                .build()

        then:
        thrown(Exception)

        cleanup:
        source?.close()

        where:
        type << DatabaseType.values()
    }

    def 'test that #type returns #dialect'() {
        given:
        def builder = SqlDataSource.builder()
                .executor(executor)
                .database('sql_data_source')
                .username('user')
                .password('password')
                .databaseType(type)

        when:
        def actual = builder.getSQLDialect()

        then:
        actual == dialect

        where:
        type                       || dialect
        DatabaseType.MYSQL         || SQLDialect.MYSQL
        DatabaseType.MARIADB       || SQLDialect.MARIADB
        DatabaseType.POSTGRES      || SQLDialect.POSTGRES
        new IDatabaseType() {

            @Override
            @NotNull
            String getJdbcName() {
                return "unknown"
            }

            @Override
            int getPort() {
                return 1337
            }

        }                          || SQLDialect.DEFAULT
    }

    /*
     * SQLite
     */

    def 'test initialize sqlite memory connection'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('sqlite_data_source')
                .username('sa')
                .password('')
                .sqlite()
                .memory()
                .build()

        then:
        noExceptionThrown()

        cleanup:
        source?.close()
    }

    def 'test initialize sqlite disk connection'() {
        given:
        def expected = new File('build/resources/test/sqlite_data_source/sqlite_data_source.db')
        expected.parentFile.mkdirs()

        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('sqlite_data_source')
                .username('sa')
                .password('')
                .sqlite()
                .disk('./build/resources/test/sqlite_data_source')
                .build()

        then:
        noExceptionThrown()

        and:
        expected.exists()

        cleanup:
        source?.close()
    }

    def 'test that SQLDialect is SQLITE'() {
        given:
        def builder = SqlDataSource.builder()
                .executor(executor)
                .database('sqlite_data_source')
                .username('sa')
                .password('')
                .sqlite()

        expect:
        builder.getSQLDialect() == SQLDialect.SQLITE
    }

    /*
     * H2
     */

    def 'test initialize h2 memory connection'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('h2_data_source')
                .username('sa')
                .password('')
                .h2()
                .memory()
                .preventMemoryLoss()
                .build()

        then:
        noExceptionThrown()

        cleanup:
        source?.close()
    }

    def 'test initialize h2 disk connection'() {
        given:
        def expected = new File('build/resources/test/h2_data_source/h2_data_source.mv.db')

        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('h2_data_source')
                .username('sa')
                .password('')
                .h2()
                .disk('./build/resources/test/h2_data_source')
                .allowSimultaneousFileConnections()
                .build()

        then:
        noExceptionThrown()

        and:
        expected.exists()

        cleanup:
        source?.close()
    }

    def 'test initialize h2 disk connection throws on non-existing'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('h2_data_source')
                .username('sa')
                .password('')
                .h2()
                .disk('./build/resources/test/h2_data_source_invalid/')
                .allowSimultaneousFileConnections()
                .preventConnectionOnNonExistingFile()
                .build()

        then:
        thrown(HikariPool.PoolInitializationException)

        cleanup:
        source?.close()
    }

    def 'test initialize h2 server connection'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('h2_data_source')
                .username('sa')
                .password('')
                .h2()
                .server('localhost', 18379)
                .build()

        then:
        thrown(HikariPool.PoolInitializationException)

        cleanup:
        source?.close()
    }

    def 'test that SQLDialect is H2'() {
        given:
        def builder = SqlDataSource.builder()
                .executor(executor)
                .database('h2_data_source')
                .username('sa')
                .password('')
                .h2()

        expect:
        builder.getSQLDialect() == SQLDialect.H2
    }

    /*
     * SQL
     */

    def 'test initialize general SQL throws'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('sql_data_source')
                .username('sa')
                .password('')
                .build()

        then:
        thrown(IllegalStateException)

        cleanup:
        source?.close()
    }

    def 'test initialize general SQL throws'() {
        when:
        def source = SqlDataSource.builder()
                .executor(executor)
                .database('sql_data_source')
                .username('sa')
                .password('')
                .getSQLDialect()

        then:
        thrown(IllegalStateException)

        cleanup:
        source?.close()
    }

}
