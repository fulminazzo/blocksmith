package it.fulminazzo.blocksmith.data.memory.config;

import it.fulminazzo.blocksmith.data.config.DataSourceConfig;
import it.fulminazzo.blocksmith.data.config.DataSourceFactories;
import it.fulminazzo.blocksmith.data.memory.MemoryDataSource;
import lombok.Value;

import java.util.concurrent.Executors;

@Value
public class MemoryDataSourceConfig implements DataSourceConfig {

    static {
        DataSourceFactories.registerFactory(
                MemoryDataSourceConfig.class,
                config -> MemoryDataSource.create(Executors.newCachedThreadPool())
        );
    }

}
