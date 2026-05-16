package it.fulminazzo.blocksmith.data.redis.config

import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfigTest
import it.fulminazzo.blocksmith.data.config.DataSourceFactory

class RedisDataSourceConfigTest extends DataSourceConfigTest {

    @Override
    protected Class<? extends DataSourceConfig> getConfigType() {
        return RedisDataSourceConfig
    }

    @Override
    protected Class<? extends DataSourceFactory> getFactoryType() {
        return RedisDataSourceFactory
    }

}
