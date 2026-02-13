package it.fulminazzo.blocksmith.data.file

import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.config.ConfigurationFormat
import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

import java.util.concurrent.Executors

@Slf4j
class FileDataSourceTest extends Specification {

    def 'test datasource life cycle'() {
        given:
        def executor = Executors.newSingleThreadExecutor()

        and:
        def dataSource = FileDataSource.create(executor)

        when:
        def repository = dataSource.newRepository(
                User,
                new File('build/resources/test'),
                log,
                ConfigurationFormat.JSON
        )

        then:
        repository != null

        when:
        dataSource.close()

        then:
        executor.isShutdown()
    }

}
