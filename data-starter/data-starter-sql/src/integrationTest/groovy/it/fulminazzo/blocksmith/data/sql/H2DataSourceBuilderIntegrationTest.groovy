package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class H2DataSourceBuilderIntegrationTest extends Specification {
    private final ExecutorService executor = Executors.newSingleThreadExecutor()
    private final HikariConfig config = new HikariConfig()

    void setup() {
        config.username = 'sa'
        config.password = ''
    }

    def 'test that database is initialized with custom schema'() {
        given:
        def builder = new H2DataSourceBuilder(config, 'test', executor)
                .memory()
                .schemaName('custom')

        when:
        def dataSource = builder.build()
        def connection = dataSource.dataSource.connection

        then:
        connection.catalog == 'TEST'
        connection.schema == 'CUSTOM'

        cleanup:
        connection?.close()
        dataSource?.close()
    }

    def 'test that init script works'() {
        given:
        def builder = new H2DataSourceBuilder(config, 'test', executor)
                .memory()
                .initScript('build/resources/integrationTest/h2_schema.sql')

        when:
        def dataSource = builder.build()
        def connection = dataSource.dataSource.connection

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

        cleanup:
        connection?.close()
        dataSource?.close()
    }

}
