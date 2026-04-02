package it.fulminazzo.blocksmith.data.config

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import it.fulminazzo.blocksmith.data.sql.DatabaseType
import it.fulminazzo.blocksmith.data.sql.SqlDataSource
import redis.embedded.RedisServer
import spock.lang.Specification

import java.util.concurrent.Executors

class DataSourceConfigExampleTest extends Specification {
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

        def source = SqlDataSource.builder()
                .executor(Executors.newCachedThreadPool())
                .database('test')
                .databaseType(DatabaseType.MARIADB)
                .mysql()
                .build()
        source.executeScriptFromResource('/schema.sql')
        source.close()
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
        DataSourceConfigExample.main(new String[0])

        then:
        noExceptionThrown()
    }

}
