package it.fulminazzo.blocksmith.data.mongodb

import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.reverse.TransitionWalker
import it.fulminazzo.blocksmith.data.User
import spock.lang.Specification

class MongoDataSourceTest extends Specification {
    private static final int serverPort = 47018

    private static TransitionWalker.ReachedState<RunningMongodProcess> server

    void setupSpec() {
        server = TestUtils.startServer(serverPort)
    }

    void cleanupSpec() {
        server?.close()
    }

    def 'test datasource life cycle'() {
        given:
        def dataSource = MongoDataSource.builder()
                .host('localhost', serverPort)
                .applicationName('mongo-datasource-test/1.0.0')
                .build()

        when:
        def repository = dataSource.newRepository(User, 'database', 'users')

        then:
        repository != null

        when:
        dataSource.close()

        then:
        noExceptionThrown()
    }

}
