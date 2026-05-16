package it.fulminazzo.blocksmith.data.sql

import spock.lang.Specification

import java.util.concurrent.Executors

class RemoteDataSourceBuilderTest extends Specification {

    def 'test that build throws for unknown database type'() {
        when:
        SqlDataSource.builder()
                .database('test')
        .executor(Executors.newSingleThreadExecutor())
                .databaseType(new IDatabaseType() {
                    @Override
                    String getJdbcName() {
                        return 'clickhouse'
                    }

                    @Override
                    int getPort() {
                        return 9000
                    }
                })
                .build()

        then:
        thrown(IllegalArgumentException)
    }

}
