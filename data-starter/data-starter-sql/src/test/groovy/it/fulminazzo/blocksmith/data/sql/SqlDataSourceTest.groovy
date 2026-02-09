package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.pool.HikariPool
import spock.lang.Specification

class SqlDataSourceTest extends Specification {

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
                .setDatabase('sql_data_source')
                .setUsername('user')
                .setPassword('password')
                .setDatabaseType(type)
                .host('localhost')
                .port(23692)
                ."${methodName}"()
                .build()

        then:
        thrown(Exception)

        cleanup:
        if (source != null) source.close()

        where:
        type << DatabaseType.values()
    }


    /*
     * H2
     */

    def 'test initialize h2 memory connection'() {
        when:
        def source = SqlDataSource.builder()
                .setDatabase('sql_data_source')
                .setUsername('sa')
                .setPassword('')
                .h2()
                .memory()
                .preventMemoryLoss()
                .build()

        then:
        noExceptionThrown()

        cleanup:
        if (source != null) source.close()
    }

    def 'test initialize h2 disk connection'() {
        when:
        def source = SqlDataSource.builder()
                .setDatabase('sql_data_source')
                .setUsername('sa')
                .setPassword('')
                .h2()
                .disk('./build/resources/test/sql_data_source')
                .allowSimultaneousFileConnections()
                .build()

        then:
        noExceptionThrown()

        cleanup:
        if (source != null) source.close()
    }

    def 'test initialize h2 disk connection throws on non-existing'() {
        when:
        def source = SqlDataSource.builder()
                .setDatabase('sql_data_source')
                .setUsername('sa')
                .setPassword('')
                .h2()
                .disk('./build/resources/test/sql_data_source_invalid/')
                .allowSimultaneousFileConnections()
                .preventConnectionOnNonExistingFile()
                .build()

        then:
        thrown(HikariPool.PoolInitializationException)

        cleanup:
        if (source != null) source.close()
    }

    def 'test initialize h2 server connection'() {
        when:
        def source = SqlDataSource.builder()
                .setDatabase('sql_data_source')
                .setUsername('sa')
                .setPassword('')
                .h2()
                .server('localhost', 18379)
                .build()

        then:
        thrown(HikariPool.PoolInitializationException)

        cleanup:
        if (source != null) source.close()
    }

}
