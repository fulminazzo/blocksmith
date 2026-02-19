package it.fulminazzo.blocksmith.data.memory


import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Executors

class MemoryDataSourceTest extends Specification {

    def 'test datasource life cycle'() {
        given:
        def executor = Executors.newSingleThreadExecutor()

        and:
        def dataSource = MemoryDataSource.create(executor)

        when:
        def repository = dataSource.newRepository(
                User,
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofSeconds(1))
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        executor.isShutdown()
    }

}
