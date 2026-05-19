package it.fulminazzo.blocksmith.data

import spock.lang.Specification

abstract class DataSourceIntegrationTest<S extends RepositorySettings> extends Specification {

    def 'test datasource life cycle'() {
        given:
        def builder = newDataSourceBuilder()

        when:
        def dataSource = builder.build()

        then:
        noExceptionThrown()

        when:
        def repository = dataSource.newRepository(User, settings)

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

    protected abstract RepositoryDataSourceBuilder<RepositoryDataSource<S>> newDataSourceBuilder()

    protected abstract S getSettings()

}
