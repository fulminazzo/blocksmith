package it.fulminazzo.blocksmith.data.config

import it.fulminazzo.blocksmith.data.MockDataSource
import spock.lang.Specification

class DataSourceFactoriesTest extends Specification {

    def 'test register-build cycle'() {
        given:
        final config = new MockDataSourceConfig()

        when:
        DataSourceFactories.registerFactory(
                config.class,
                new MockDataSourceFactory()
        )

        then:
        noExceptionThrown()

        when:
        def dataSource = DataSourceFactories.build(config)

        then:
        MockDataSource.isInstance(dataSource)

        cleanup:
        dataSource?.close()
    }

    def 'test that build of not found data source configuration type throws'() {
        when:
        DataSourceFactories.build(Mock(DataSourceConfig))

        then:
        thrown(IllegalArgumentException)
    }

}
