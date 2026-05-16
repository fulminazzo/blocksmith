package it.fulminazzo.blocksmith.data

import spock.lang.Specification

class RepositoryDataSourceTest extends Specification {

    def 'test that newRepository of raw type delegates with entity mapper'() {
        given:
        def dataSource = new MockDataSource()

        when:
        def repository = dataSource.newRepository(User, new MockRepositorySettings())

        then:
        repository != null

        cleanup:
        dataSource?.close()
    }

}
