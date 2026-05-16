package it.fulminazzo.blocksmith.data.memory.config

import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfigTest
import it.fulminazzo.blocksmith.data.config.DataSourceFactory

class MemoryDataSourceConfigTest extends DataSourceConfigTest {

    @Override
    protected Class<? extends DataSourceConfig> getConfigType() {
        return MemoryDataSourceConfig
    }

    @Override
    protected Class<? extends DataSourceFactory> getFactoryType() {
        return DataSourceFactory
    }

}
