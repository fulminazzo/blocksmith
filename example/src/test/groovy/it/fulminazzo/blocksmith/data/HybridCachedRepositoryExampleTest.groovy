package it.fulminazzo.blocksmith.data

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import redis.embedded.RedisServer
import spock.lang.Specification

class HybridCachedRepositoryExampleTest extends Specification {
    private static DB mariadb
    private static RedisServer redis

    void setupSpec() {
        mariadb = DB.newEmbeddedDB(
                DBConfigurationBuilder.newBuilder()
                        .setPort(3306)
                        .setDataDir(new File('build/resources/test/mariadb'))
                        .build()
        )
        mariadb.start()

        redis = new RedisServer()
        redis.start()
    }

    void cleanupSpec() {
        redis?.stop()
        mariadb?.stop()
    }

    def 'test that servers are online'() {
        expect:
        mariadb.mysqldProcess.isAlive()

        and:
        redis.active
    }

    def 'test that main functioning does not throw'() {
        when:
        HybridCachedRepositoryExample.main(new String[0])

        then:
        noExceptionThrown()
    }

}
