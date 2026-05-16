package it.fulminazzo.blocksmith.data.mongodb.config

import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfigTest
import it.fulminazzo.blocksmith.data.config.DataSourceFactory

class MongoDataSourceConfigTest extends DataSourceConfigTest {

    @Override
    protected Class<? extends DataSourceConfig> getConfigType() {
        return MongoDataSourceConfig
    }

    @Override
    protected Class<? extends DataSourceFactory> getFactoryType() {
        return MongoDataSourceFactory
    }

}
