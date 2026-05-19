package it.fulminazzo.blocksmith.data.cache.config

import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfigTest
import it.fulminazzo.blocksmith.data.config.DataSourceFactory

class CachedDataSourceConfigTest extends DataSourceConfigTest {

    @Override
    protected Class<? extends DataSourceConfig> getConfigType() {
        return CachedDataSourceConfig
    }

    @Override
    protected Class<? extends DataSourceFactory> getFactoryType() {
        return DataSourceFactory
    }

}
