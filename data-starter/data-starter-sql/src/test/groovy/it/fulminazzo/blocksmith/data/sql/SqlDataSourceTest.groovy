package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.pool.HikariPool
import spock.lang.Specification

class SqlDataSourceTest extends Specification {

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
