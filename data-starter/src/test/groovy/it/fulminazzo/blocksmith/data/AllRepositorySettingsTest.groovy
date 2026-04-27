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

    private final static MemoryRepositorySettings memory = new MemoryRepositorySettings()
    private final static FileRepositorySettings file = new FileRepositorySettings()
    private final static SqlRepositorySettings sql = new SqlRepositorySettings()
    private final static RedisRepositorySettings redis = new RedisRepositorySettings()
    private final static MongoRepositorySettings mongo = new MongoRepositorySettings()

    private final static AllRepositorySettings settings = AllRepositorySettings.builder()
            .memory(memory)
            .file(file)
            .sql(sql)
            .redis(redis)
            .mongo(mongo)
            .build()

    def 'test that getRepositorySettings returns #expected with #dataSource'() {
        when:
        def actual = settings.getRepositorySettings(dataSource)

        then:
        actual == expected

        where:
        dataSource                                                            || expected
        Mock(MemoryDataSource)                                                || memory
        Mock(FileDataSource)                                                  || file
        Mock(SqlDataSource)                                                   || sql
        Mock(RedisDataSource)                                                 || redis
        Mock(MongoDataSource)                                                 || mongo
        new CachedDataSource<>(Mock(MemoryDataSource), Mock(MongoDataSource)) || CachedRepositorySettings.combine(memory, mongo)
    }

}
