package it.fulminazzo.blocksmith.data.sql

import com.zaxxer.hikari.HikariConfig
import spock.lang.Specification

class H2DataSourceBuilderTest extends Specification {

    private HikariConfig config

    void setup() {
        config = new HikariConfig()
        config.username = 'sa'
        config.password = ''
    }

    def 'test that database is initialized with custom schema'() {
        given:
        def builder = new H2DataSourceBuilder(config, 'test')
                .memory()
                .schemaName('custom')

        when:
        def source = builder.build()
        def connection = source.getConnection()

        then:
        connection.catalog == 'TEST'
        connection.schema == 'CUSTOM'

        cleanup:
        if (connection != null) connection.close()
        if (source != null) source.close()
    }

    def 'test that init script works'() {
        given:
        def builder = new H2DataSourceBuilder(config, 'test')
                .memory()
                .initScript('build/resources/test/h2_schema.sql')

        when:
        def source = builder.build()
        def connection = source.getConnection()

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

}
