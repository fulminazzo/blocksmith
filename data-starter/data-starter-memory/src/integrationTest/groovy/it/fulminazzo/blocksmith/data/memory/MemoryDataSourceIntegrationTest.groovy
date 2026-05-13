package it.fulminazzo.blocksmith.data.memory

import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

import java.time.Duration

class MemoryDataSourceIntegrationTest extends Specification {

    def 'test datasource life cycle'() {
        given:
        def dataSource = MemoryDataSource.create()

        when:
        def repository = dataSource.newRepository(
                User,
                new MemoryRepositorySettings()
                        .withTtl(Duration.ofSeconds(1))
                        .withExpirationStrategy(MemoryRepositorySettings.ExpiryStrategy.SCHEDULED)
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        dataSource.executor.shutdown
    }

}
