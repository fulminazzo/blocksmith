package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

import java.util.concurrent.Executors

@Slf4j
class FileDataSourceIntegrationTest extends Specification {

    def 'test datasource life cycle'() {
        given:
        def executor = Executors.newSingleThreadExecutor()

        and:
        def dataSource = FileDataSource.create(executor)

        when:
        def repository = dataSource.newRepository(
                User,
                new FileRepositorySettings()
                        .withDataDirectory(new File('build/resources/integrationTest'))
                        .withLogger(log)
                        .withFormat(ConfigurationFormat.JSON)
        )

        then:
        repository != null

        when:
        def user = repository.findById(1L).join()

        then:
        user.empty

        when:
        dataSource.close()

        then:
        executor.shutdown
    }

}
