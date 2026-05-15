package it.fulminazzo.blocksmith.data.sql

import spock.lang.Specification

class ASqlDataSourceBuilderTest extends Specification {

    def 'test initialize general SQL throws'() {
        given:
        def builder = SqlDataSource.builder()
                .database('sql_data_source')
                .username('root')
                .password('test')

        when:
        builder.build()

        then:
        thrown(IllegalStateException)

        when:
        builder.SQLDialect

        then:
        thrown(IllegalStateException)
    }

}
