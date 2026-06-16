package it.fulminazzo.blocksmith.data

import it.fulminazzo.blocksmith.data.cache.CachedDataSource
import it.fulminazzo.blocksmith.data.cache.CachedRepositorySettings
import it.fulminazzo.blocksmith.data.file.FileDataSource
import it.fulminazzo.blocksmith.data.file.FileRepositorySettings
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource
import it.fulminazzo.blocksmith.data.memory.MemoryRepositorySettings
import it.fulminazzo.blocksmith.data.mongodb.MongoDataSource
import it.fulminazzo.blocksmith.data.mongodb.MongoRepositorySettings
import it.fulminazzo.blocksmith.data.redis.RedisDataSource
import it.fulminazzo.blocksmith.data.redis.RedisRepositorySettings
import it.fulminazzo.blocksmith.data.sql.SqlDataSource
import it.fulminazzo.blocksmith.data.sql.SqlRepositorySettings
import spock.lang.Specification

class AllRepositorySettingsTest extends Specification {
    private static final MemoryRepositorySettings MEMORY = new MemoryRepositorySettings()
    private static final FileRepositorySettings FILE = new FileRepositorySettings()
    private static final SqlRepositorySettings SQL = new SqlRepositorySettings()
    private static final RedisRepositorySettings REDIS = new RedisRepositorySettings()
    private static final MongoRepositorySettings MONGO = new MongoRepositorySettings()

    private static final AllRepositorySettings SETTINGS = AllRepositorySettings.builder()
            .memory(MEMORY)
            .file(FILE)
            .sql(SQL)
            .redis(REDIS)
            .mongo(MONGO)
            .build()

    def 'test that getRepositorySettings returns #expected with #dataSource'() {
        when:
        def actual = SETTINGS.getRepositorySettings(dataSource)

        then:
        actual == expected

        where:
        dataSource                                                            || expected
        Mock(MemoryDataSource)                                                || MEMORY
        Mock(FileDataSource)                                                  || FILE
        Mock(SqlDataSource)                                                   || SQL
        Mock(RedisDataSource)                                                 || REDIS
        Mock(MongoDataSource)                                                 || MONGO
        new CachedDataSource<>(Mock(MemoryDataSource), Mock(MongoDataSource)) || CachedRepositorySettings.combine(MEMORY, MONGO)
    }

    def 'test that getRepositorySettings throws for unrecognized data source'() {
        when:
        SETTINGS.getRepositorySettings(Mock(RepositoryDataSource))

        then:
        thrown(IllegalArgumentException)
    }

}
