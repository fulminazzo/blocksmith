package it.fulminazzo.blocksmith.data.sql

import spock.lang.Specification

import java.util.concurrent.Executors

class RemoteDataSourceBuilderTest extends Specification {

    def 'test that build throws for unknown database type'() {
        given:
        final databaseType = new IDatabaseType() {
            final String jdbcName = 'clickhouse'
            final int port = 9000

        }

        when:
        SqlDataSource.builder()
                .database('test')
                .executor(Executors.newSingleThreadExecutor())
                .databaseType(databaseType)
                .build()

        then:
        thrown(IllegalArgumentException)
    }

}
