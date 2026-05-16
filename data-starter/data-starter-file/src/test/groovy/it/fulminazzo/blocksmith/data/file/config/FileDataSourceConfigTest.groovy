package it.fulminazzo.blocksmith.data.file.config

import it.fulminazzo.blocksmith.data.config.DataSourceConfig
import it.fulminazzo.blocksmith.data.config.DataSourceConfigTest
import it.fulminazzo.blocksmith.data.config.DataSourceFactory

class FileDataSourceConfigTest extends DataSourceConfigTest {

    @Override
    protected Class<? extends DataSourceConfig> getConfigType() {
        return FileDataSourceConfig
    }

    @Override
    protected Class<? extends DataSourceFactory> getFactoryType() {
        return DataSourceFactory
    }

}
